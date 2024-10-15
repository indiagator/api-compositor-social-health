package com.egov.socialservice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RequestIdExtractor
{

    public  String getRequestId(HttpServletRequest request) {
        // Check for the request ID header
        String requestId = request.getHeader("X-Request-ID");

        // If the header is not present, check for other common header names
        if (requestId == null) {
            requestId = request.getHeader("X-Amzn-Trace-Id");
        }

        if (requestId == null) {
            requestId = request.getHeader("X-Correlation-ID");
        }

        // If the request ID is still null, you can generate a unique ID
        if (requestId == null) {
            requestId = generateUniqueId();
        }

        return requestId;
    }

    private  String generateUniqueId() {
        // Generate a unique ID using a UUID or a custom logic
        return UUID.randomUUID().toString();
    }
}