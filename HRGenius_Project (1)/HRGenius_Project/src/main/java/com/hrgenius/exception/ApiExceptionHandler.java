package com.hrgenius.exception;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<?> missing(Exception e){return ResponseEntity.status(404).body(Map.of("error","NOT_FOUND","message",e.getMessage()));}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> bad(Exception e){return ResponseEntity.badRequest().body(Map.of("error","BAD_REQUEST","message",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("error","VALIDATION_ERROR","message","Request validation failed"));}
 @ExceptionHandler(Exception.class) ResponseEntity<?> internal(Exception e){return ResponseEntity.status(500).body(Map.of("error","INTERNAL_ERROR","message","An unexpected error occurred"));}
}
