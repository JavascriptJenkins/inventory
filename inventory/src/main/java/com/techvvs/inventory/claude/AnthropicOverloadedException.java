// AnthropicOverloadedException.java
package com.techvvs.inventory.claude;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

public class AnthropicOverloadedException extends RuntimeException {
    public AnthropicOverloadedException(String msg) { super(msg); }

    // GlobalExceptionHandlerController.java (snippet)
    @ExceptionHandler(AnthropicOverloadedException.class)
    public ResponseEntity<?> handleOverloaded(AnthropicOverloadedException ex) {
        return ResponseEntity.status(503).body(Map.of(
                "error", "Claude overloaded",
                "message", ex.getMessage()
        ));
    }

}
