package io.joshuasalcedo.commonlibs.domain;

import com.netflix.discovery.shared.transport.TransportException;
import io.joshuasalcedo.commonlibs.domain.base.dto.ErrorResponseDTO;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to provide consistent error responses across the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private LoggingService logger;

    /**
     * Check if request accepts or expects SSE (text/event-stream)
     */
    private boolean isEventStreamRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        String contentType = request.getContentType();

        return (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) ||
                (contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    /**
     * Handle SSE-specific error response
     */
    private ResponseEntity<String> handleEventStreamError(HttpStatus status, String message) {
        String eventData = "event: error\ndata: {\"status\":" + status.value() +
                ",\"message\":\"" + message.replace("\"", "\\\"") +
                "\"}\n\n";

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventData);
    }

    /**
     * Handle ResourceNotFoundException.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        logger.warn("Resource not found: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

/**
 * Handle TransportException.
 * These typically occur when there are issues with network communication
 * or when data transfer between services fails.
 */
@ExceptionHandler(TransportException.class)
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public ResponseEntity<?> handleTransportException(
        TransportException ex, HttpServletRequest request) {
    logger.error("Transport exception occurred: {}", ex.getMessage(), ex);
    
    if (isEventStreamRequest(request)) {
        return handleEventStreamError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
    
    ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
    
    // Include root cause if available
    if (ex.getCause() != null) {
        errorResponse.setDetails(Map.of("cause : ", ex.getCause().getMessage()));
    }
    
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
}

    /**
     * Handle BadRequestException.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {

        logger.warn("Bad request: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnauthorizedException.
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {

        logger.warn("Unauthorized access: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle ForbiddenException.
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<?> handleForbiddenException(
            ForbiddenException ex, HttpServletRequest request) {

        logger.warn("Forbidden access: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.FORBIDDEN, ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle ConflictException.
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<?> handleConflictException(
            ConflictException ex, HttpServletRequest request) {

        logger.warn("Data conflict: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.CONFLICT, ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle validation exceptions.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleValidationExceptions(
            Exception ex, HttpServletRequest request) {

        List<FieldError> fieldErrors = new ArrayList<>();

        if (ex instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors();
        } else if (ex instanceof BindException) {
            fieldErrors = ((BindException) ex).getBindingResult().getFieldErrors();
        }

        List<ErrorResponseDTO.ValidationError> validationErrors = fieldErrors.stream()
                .map(fieldError -> ErrorResponseDTO.ValidationError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .rejectedValue(fieldError.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        logger.warn("Validation error: {}", validationErrors);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, "Validation failed");
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions from validation annotations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorResponseDTO.ValidationError> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String field = propertyPath.contains(".") ?
                            propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;

                    return ErrorResponseDTO.ValidationError.builder()
                            .field(field)
                            .message(violation.getMessage())
                            .rejectedValue(violation.getInvalidValue())
                            .build();
                })
                .collect(Collectors.toList());

        logger.warn("Constraint violation: {}", validationErrors);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, "Validation constraints violated");
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation constraints violated")
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        logger.warn("Type mismatch: {}", message);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        logger.warn("Missing parameter: {}", message);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle HttpMessageNotReadable exception.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = "Malformed JSON request";
        logger.warn("Message not readable: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.BAD_REQUEST, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle HttpMediaTypeNotSupported exception.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        String message = String.format("Media type '%s' is not supported", ex.getContentType());
        logger.warn("Unsupported media type: {}", message);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle HttpRequestMethodNotSupported exception.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<?> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String message = String.format("Method '%s' is not supported for this request", ex.getMethod());
        logger.warn("Method not allowed: {}", message);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.METHOD_NOT_ALLOWED, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle MaxUploadSizeExceededException.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        String message = "File size exceeds the maximum allowed upload size";
        logger.warn("Max upload size exceeded: {}", ex.getMessage());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.PAYLOAD_TOO_LARGE, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handle NoHandlerFoundException to replace the white label error page.
     * This requires setting spring.mvc.throw-exception-if-no-handler-found=true
     * and spring.web.resources.add-mappings=false in application properties.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.NOT_FOUND, "The requested resource is not available: " + ex.getRequestURL());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message("The requested resource is not available: " + ex.getRequestURL())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle async request timeout.
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<?> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpServletRequest request) {

        logger.error("Async request timeout: {}", ex.getMessage());

        String message = "Request timed out. The server is currently unable to handle the request. Please try again later.";

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.SERVICE_UNAVAILABLE, message);
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle HttpMessageNotWritableException specially.
     * This is what's causing your SSE content type issue.
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleHttpMessageNotWritableException(
            HttpMessageNotWritableException ex,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.error("Message not writable: {}", ex.getMessage());

        // For SSE, we need to write directly to the response
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (isEventStreamRequest(request)) {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            String eventData = "event: error\ndata: {\"status\":" + HttpStatus.INTERNAL_SERVER_ERROR.value() +
                    ",\"message\":\"Internal server error\"}\n\n";
            response.getOutputStream().write(eventData.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .message("Internal server error")
                    .path(request.getRequestURI())
                    .build();

            String jsonResponse = "{\"timestamp\":\"" + errorResponse.getTimestamp() + "\"," +
                    "\"status\":" + errorResponse.getStatus() + "," +
                    "\"error\":\"" + errorResponse.getError() + "\"," +
                    "\"message\":\"" + errorResponse.getMessage() + "\"," +
                    "\"path\":\"" + errorResponse.getPath() + "\"}";

            response.getOutputStream().write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        }
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unhandled exception: {}", ex.getMessage(), ex);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle IOException for network and file operations.
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleIOException(
            IOException ex, HttpServletRequest request) {

        // Check for broken pipe or connection reset - client disconnected
        if (ex.getMessage() != null &&
                (ex.getMessage().contains("Broken pipe") ||
                        ex.getMessage().contains("Connection reset"))) {

            logger.info("Client disconnected: {}", ex.getMessage());
            // Return null for client disconnection - response won't be delivered anyway
            return null;
        }

        // Otherwise, log as a server error
        logger.error("I/O error: {}", ex.getMessage(), ex);

        if (isEventStreamRequest(request)) {
            return handleEventStreamError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "A network or file operation error occurred: " + ex.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("A network or file operation error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
