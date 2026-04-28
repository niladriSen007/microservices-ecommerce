package com.niladri.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic, production-ready API response wrapper.
 *
 * <p>
 * Success shape:
 * 
 * <pre>
 * {
 *   "success": true,
 *   "statusCode": 200,
 *   "message": "...",
 *   "data": { ... },
 *   "timestamp": "2026-04-28T..."
 * }
 * </pre>
 *
 * <p>
 * Failure shape:
 * 
 * <pre>
 * {
 *   "success": false,
 *   "statusCode": 400,
 *   "message": "...",
 *   "errors": { "field": "reason" },
 *   "timestamp": "2026-04-28T..."
 * }
 * </pre>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int statusCode;
    private String message;
    private T data;

    /**
     * Holds validation field-errors (Map&lt;String,String&gt;) or a plain error
     * string.
     * Omitted from JSON when null (success responses).
     */
    private Object errors;

    private Instant timestamp;

    // ── Static factory helpers ────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, int statusCode, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .errors(errors)
                .timestamp(Instant.now())
                .build();
    }
}
