package io.joshuasalcedo.commonlibs.controller;

import io.joshuasalcedo.commonlibs.autoconfigure.SseLiveLogAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for Server-Sent Events based live logs.
 */
@Controller
@RequestMapping("/logs")
public class SseLiveLogsController {

    private final SseLiveLogAutoConfiguration logConfig;

    public SseLiveLogsController(SseLiveLogAutoConfiguration logConfig) {
        this.logConfig = logConfig;
    }

    /**
     * Endpoint for establishing SSE connection
     * @return SSE emitter for streaming logs
     */
    @GetMapping("/stream")
    public SseEmitter streamLogs() {
        return logConfig.registerEmitter();
    }

    /**
     * Returns the HTML page for viewing live logs via SSE.
     *
     * @return HTML content for live logs viewer
     */
    @GetMapping("/live-sse")
    @ResponseBody
    public String getLiveLogsPage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Live Application Logs (SSE)</title>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Courier New', monospace;
                            background-color: #1e1e1e;
                            color: #f0f0f0;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 100%;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #333;
                            padding: 10px 20px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            position: sticky;
                            top: 0;
                            z-index: 1000;
                        }
                        h1 {
                            margin: 0;
                            font-size: 1.5rem;
                        }
                        .controls {
                            display: flex;
                            gap: 10px;
                        }
                        button {
                            background-color: #0066cc;
                            color: white;
                            border: none;
                            padding: 8px 12px;
                            cursor: pointer;
                            border-radius: 4px;
                        }
                        button:hover {
                            background-color: #0056b3;
                        }
                        select {
                            padding: 8px;
                            border-radius: 4px;
                        }
                        .connection-status {
                            display: inline-block;
                            padding: 4px 8px;
                            border-radius: 4px;
                            font-size: 12px;
                            margin-left: 10px;
                        }
                        .connected {
                            background-color: #2e8b57;
                        }
                        .disconnected {
                            background-color: #cd5c5c;
                        }
                        .log-window {
                            background-color: #252526;
                            border: 1px solid #3c3c3c;
                            border-radius: 4px;
                            padding: 10px;
                            height: calc(100vh - 120px);
                            overflow-y: auto;
                            margin-top: 20px;
                            font-size: 14px;
                        }
                        .log-entry {
                            padding: 4px 6px;
                            border-bottom: 1px solid #333;
                            white-space: pre-wrap;
                            word-break: break-word;
                        }
                        .timestamp {
                            color: #5f9ea0;
                        }
                        .logger {
                            color: #9acd32;
                            font-weight: bold;
                        }
                        .thread {
                            color: #daa520;
                        }
                        .TRACE {
                            color: #808080;
                        }
                        .DEBUG {
                            color: #1e90ff;
                        }
                        .INFO {
                            color: #3cb371;
                        }
                        .WARN {
                            color: #ffa500;
                        }
                        .ERROR {
                            color: #ff6347;
                            font-weight: bold;
                        }
                        .search-bar {
                            margin: 10px 0;
                            width: 100%;
                        }
                        .search-bar input {
                            width: 100%;
                            padding: 8px;
                            border-radius: 4px;
                            border: 1px solid #3c3c3c;
                            background-color: #2d2d2d;
                            color: #f0f0f0;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div>
                            <h1>Live Application Logs</h1>
                            <span id="connectionStatus" class="connection-status disconnected">Disconnected</span>
                        </div>
                        <div class="controls">
                            <select id="logLevel">
                                <option value="TRACE">TRACE</option>
                                <option value="DEBUG">DEBUG</option>
                                <option value="INFO" selected>INFO</option>
                                <option value="WARN">WARN</option>
                                <option value="ERROR">ERROR</option>
                            </select>
                            <button id="clearLogs">Clear Logs</button>
                            <button id="pauseResume">Pause</button>
                            <button id="reconnect">Reconnect</button>
                            <button id="scrollToBottom">Scroll to Bottom</button>
                        </div>
                    </div>
                    
                    <div class="container">
                        <div class="search-bar">
                            <input type="text" id="searchInput" placeholder="Filter logs (press Enter to apply)">
                        </div>
                        <div class="log-window" id="logWindow"></div>
                    </div>
                
                    <script>
                        document.addEventListener('DOMContentLoaded', function() {
                            const logWindow = document.getElementById('logWindow');
                            const logLevelSelect = document.getElementById('logLevel');
                            const clearLogsButton = document.getElementById('clearLogs');
                            const pauseResumeButton = document.getElementById('pauseResume');
                            const reconnectButton = document.getElementById('reconnect');
                            const scrollToBottomButton = document.getElementById('scrollToBottom');
                            const searchInput = document.getElementById('searchInput');
                            const connectionStatus = document.getElementById('connectionStatus');
                            
                            let isPaused = false;
                            let autoScroll = true;
                            let eventSource = null;
                            let filterText = '';
                            
                            // Create SSE connection
                            function connect() {
                                if (eventSource) {
                                    eventSource.close();
                                }
                                
                                eventSource = new EventSource('/logs/stream');
                                
                                eventSource.onopen = function() {
                                    connectionStatus.textContent = 'Connected';
                                    connectionStatus.classList.remove('disconnected');
                                    connectionStatus.classList.add('connected');
                                    console.log('SSE connection established');
                                };
                                
                                eventSource.addEventListener('log', function(event) {
                                    if (!isPaused) {
                                        const logData = JSON.parse(event.data);
                                        appendLog(logData);
                                    }
                                });
                                
                                eventSource.onerror = function() {
                                    connectionStatus.textContent = 'Disconnected';
                                    connectionStatus.classList.remove('connected');
                                    connectionStatus.classList.add('disconnected');
                                    console.log('SSE connection error - will try to reconnect');
                                    
                                    // Close and retry after a delay
                                    eventSource.close();
                                    setTimeout(connect, 5000);
                                };
                            }
                            
                            function appendLog(logData) {
                                // Check if log level matches selected filter
                                const selectedLevel = logLevelSelect.value;
                                const logLevelValue = getLogLevelValue(logData.level);
                                const selectedLevelValue = getLogLevelValue(selectedLevel);
                                
                                if (logLevelValue < selectedLevelValue) {
                                    return;
                                }
                                
                                // Check if log matches text filter
                                if (filterText && !logData.message.toLowerCase().includes(filterText.toLowerCase()) &&
                                    !logData.logger.toLowerCase().includes(filterText.toLowerCase())) {
                                    return;
                                }
                                
                                const logEntry = document.createElement('div');
                                logEntry.className = 'log-entry';
                                
                                logEntry.innerHTML = `
                                    <span class="timestamp">[${logData.timestamp}]</span>
                                    <span class="${logData.level}">[ ${logData.level} ]</span>
                                    <span class="thread">[${logData.thread}]</span>
                                    <span class="logger">${shortenLogger(logData.logger)}</span>: ${escapeHtml(logData.message)}
                                `;
                                
                                if (logData.exception) {
                                    const exceptionDiv = document.createElement('div');
                                    exceptionDiv.style.color = '#ff6347';
                                    exceptionDiv.style.paddingLeft = '20px';
                                    exceptionDiv.textContent = logData.exception;
                                    logEntry.appendChild(exceptionDiv);
                                    
                                    if (logData.stackTrace) {
                                        const stackTraceDiv = document.createElement('pre');
                                        stackTraceDiv.style.color = '#ff6347';
                                        stackTraceDiv.style.paddingLeft = '20px';
                                        stackTraceDiv.style.fontSize = '12px';
                                        stackTraceDiv.style.overflow = 'auto';
                                        logData.stackTrace.forEach(trace => {
                                            stackTraceDiv.textContent += "  at " + trace + "\\n";
                                        });
                                        logEntry.appendChild(stackTraceDiv);
                                    }
                                }
                                
                                logWindow.appendChild(logEntry);
                                
                                // Limit the number of log entries to prevent browser performance issues
                                while (logWindow.children.length > 1000) {
                                    logWindow.removeChild(logWindow.firstChild);
                                }
                                
                                if (autoScroll) {
                                    logWindow.scrollTop = logWindow.scrollHeight;
                                }
                            }
                            
                            function getLogLevelValue(level) {
                                switch(level) {
                                    case 'TRACE': return 0;
                                    case 'DEBUG': return 1;
                                    case 'INFO': return 2;
                                    case 'WARN': return 3;
                                    case 'ERROR': return 4;
                                    default: return 2;
                                }
                            }
                            
                            function shortenLogger(logger) {
                                if (logger.length > 30) {
                                    const parts = logger.split('.');
                                    let shortened = '';
                                    for (let i = 0; i < parts.length - 1; i++) {
                                        shortened += parts[i].charAt(0) + '.';
                                    }
                                    return shortened + parts[parts.length - 1];
                                }
                                return logger;
                            }
                            
                            function escapeHtml(text) {
                                return text
                                    .replace(/&/g, "&amp;")
                                    .replace(/</g, "&lt;")
                                    .replace(/>/g, "&gt;")
                                    .replace(/"/g, "&quot;")
                                    .replace(/'/g, "&#039;");
                            }
                            
                            // Event Listeners
                            clearLogsButton.addEventListener('click', function() {
                                logWindow.innerHTML = '';
                            });
                            
                            pauseResumeButton.addEventListener('click', function() {
                                isPaused = !isPaused;
                                pauseResumeButton.textContent = isPaused ? 'Resume' : 'Pause';
                            });
                            
                            reconnectButton.addEventListener('click', function() {
                                connect();
                            });
                            
                            scrollToBottomButton.addEventListener('click', function() {
                                logWindow.scrollTop = logWindow.scrollHeight;
                                autoScroll = true;
                            });
                            
                            logWindow.addEventListener('scroll', function() {
                                // User manually scrolled up, disable auto-scroll
                                const isScrolledToBottom = logWindow.scrollHeight - logWindow.clientHeight <= logWindow.scrollTop + 50;
                                autoScroll = isScrolledToBottom;
                            });
                            
                            searchInput.addEventListener('keyup', function(event) {
                                if (event.key === 'Enter') {
                                    filterText = searchInput.value;
                                    // Clear and reapply the filter
                                    logWindow.innerHTML = '';
                                }
                            });
                            
                            // Initialize SSE connection
                            connect();
                        });
                    </script>
                </body>
                </html>
                """;
    }
}