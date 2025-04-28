package io.joshuasalcedo.commonlibs.domain.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic response wrapper for API responses.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    private T data;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Create a successful response with data.
     *
     * @param data the data to include in the response
     * @param message the success message
     * @param <T> the type of data
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response with data and a default message.
     *
     * @param data the data to include in the response
     * @param <T> the type of data
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Create an error response.
     *
     * @param message the error message
     * @param <T> the type of data (will be null)
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> error(String message) {
        return ResponseDTO.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}