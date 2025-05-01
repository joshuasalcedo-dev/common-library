# Live Logs Endpoints

## Web UI Endpoints
| Endpoint                   | Method | Description                                        |
|----------------------------|--------|----------------------------------------------------|
| `/logs/live`               | GET    | WebSocket-based live logs viewer UI                |
| `/logs/live-sse`           | GET    | Server-Sent Events based live logs viewer UI       |

## Server-Sent Events Endpoints
| Endpoint                   | Method | Description                                        |
|----------------------------|--------|----------------------------------------------------|
| `/logs/stream`             | GET    | SSE endpoint that streams logs in real-time        |

## WebSocket Endpoints
| Endpoint                   | Method | Description                                        |
|----------------------------|--------|----------------------------------------------------|
| `/ws-logs`                 | WS     | WebSocket connection endpoint for logs             |
| `/topic/logs`              | SUB    | WebSocket topic to subscribe for log messages      |

## Log Access REST Endpoints
| Endpoint                   | Method | Description                                        |
|----------------------------|--------|----------------------------------------------------|
| `/api/logs/list`           | GET    | Lists all available log files with metadata        |
| `/api/logs/download`       | GET    | Downloads the current log file                     |
| `/api/logs/tail`           | GET    | Returns the last 100 lines of the log file         |
| `/api/logs/stream`         | GET    | Streams log content as plain text                  |

## Spring Boot Actuator Endpoints
| Endpoint                   | Method | Description                                        |
|----------------------------|--------|----------------------------------------------------|
| `/actuator/logfile`        | GET    | Access the raw log file content                    |
| `/actuator/loggers`        | GET    | Get all configured loggers and their levels        |
| `/actuator/loggers/{name}` | GET    | Get a specific logger's configuration              |
| `/actuator/loggers/{name}` | POST   | Change a logger's level at runtime                 |
| `/actuator/health`         | GET    | Application health information                     |
| `/actuator/info`           | GET    | Application information                            |
| `/actuator/metrics`        | GET    | Application metrics                                |
| `/actuator/prometheus`     | GET    | Prometheus metrics                                 |

# Examples of Usage

## Viewing Logs
- Open your browser to view logs in real-time:
    - WebSocket Version: `http://localhost:8090/logs/live`
    - SSE Version: `http://localhost:8090/logs/live-sse`

## Changing Log Levels Programmatically
```bash
# Set io.joshuasalcedo package to DEBUG level
curl -X POST http://localhost:8090/actuator/loggers/io.joshuasalcedo \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel": "DEBUG"}'
```

## Downloading Log File
```bash
# Download current log file
curl -O http://localhost:8090/api/logs/download
```

## Tail Log File
```bash
# Get last 100 lines
curl http://localhost:8090/api/logs/tail
```

## View Available Log Files
```bash
# List all log files
curl http://localhost:8090/api/logs/list
```

## Stream Raw Logs
```bash
# Stream logs as plain text
curl http://localhost:8090/api/logs/stream
```