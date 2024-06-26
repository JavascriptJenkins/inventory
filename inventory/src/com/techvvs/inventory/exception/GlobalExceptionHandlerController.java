package com.techvvs.inventory.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

//  @Bean
//  public ErrorAttributes errorAttributes() {
//    // Hide exception field in the return object
//    return new DefaultErrorAttributes() {
//      @Override
//      public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
//        Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
//        errorAttributes.remove("exception");
//        return errorAttributes;
//      }
//    };
//  }

  @ExceptionHandler(CustomException.class)
  public void handleCustomException(HttpServletResponse res, CustomException ex) throws IOException {
    res.sendError(ex.getHttpStatus().value(), ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public void handleAccessDeniedException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
  }

  @ExceptionHandler(Exception.class)
  public void handleException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.BAD_REQUEST.value(), "Something went wrong");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public void handleDataIntegrityViolationException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.BAD_REQUEST.value(), "Data constraint violation");
  }

}
