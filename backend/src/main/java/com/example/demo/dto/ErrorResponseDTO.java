package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int           status;
    private String        error;
    private String        message;
    private String        path;
    private Map<String, String> validationErrors;

    public ErrorResponseDTO() {}
    public LocalDateTime getTimestamp()  { return timestamp; }
    public void setTimestamp(LocalDateTime v) { this.timestamp = v; }
    public int  getStatus()              { return status; }
    public void setStatus(int v)         { this.status = v; }
    public String getError()             { return error; }
    public void setError(String v)       { this.error = v; }
    public String getMessage()           { return message; }
    public void setMessage(String v)     { this.message = v; }
    public String getPath()              { return path; }
    public void setPath(String v)        { this.path = v; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> v) { this.validationErrors = v; }
}
