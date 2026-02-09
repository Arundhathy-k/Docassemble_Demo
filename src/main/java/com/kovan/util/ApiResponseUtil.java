package com.kovan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for API responses
 */
@Slf4j
@UtilityClass
public class ApiResponseUtil {

    /**
     * Creates a success response map
     */
    public static Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Creates a success response with data
     */
    public static Map<String, Object> successResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = successResponse(message);
        response.putAll(data);
        return response;
    }

    /**
     * Creates an error response map
     */
    public static Map<String, Object> errorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Creates an error response with details
     */
    public static Map<String, Object> errorResponse(String errorMessage, Throwable exception) {
        Map<String, Object> response = errorResponse(errorMessage);
        response.put("exceptionClass", exception.getClass().getSimpleName());
        response.put("exceptionMessage", exception.getMessage());
        return response;
    }

    /**
     * Converts an object to a map
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(obj, Map.class);
    }
}
