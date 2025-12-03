package com.microsoft.recommendation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class ApiErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public ApiErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        ServletWebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> attrs = errorAttributes.getErrorAttributes(
                webRequest,
                ErrorAttributeOptions.defaults()
                        .including(ErrorAttributeOptions.Include.MESSAGE)
                        .including(ErrorAttributeOptions.Include.EXCEPTION)
                        .including(ErrorAttributeOptions.Include.BINDING_ERRORS)
        );

        int status = (int) attrs.getOrDefault("status", 500);
        String error = String.valueOf(attrs.getOrDefault("error", "Unexpected Error"));
        String message = String.valueOf(attrs.getOrDefault("message", ""));
        String path = String.valueOf(attrs.getOrDefault("path", request.getRequestURI()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("hint", "Check HTTP method and endpoint. POST /ingest, GET /recommend, GET /health.");

        return ResponseEntity.status(status).body(body);
    }
}
