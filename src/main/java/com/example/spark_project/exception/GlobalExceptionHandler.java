package com.example.spark_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //handler for spring validations (ex. @NotNull, @Email etc.)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserAlreadyExistsException.class})
    public Map<String, String> handleExistingUser(UserAlreadyExistsException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidRepeatedPasswordException.class})
    public Map<String, String> handlePasswordMismatch(InvalidRepeatedPasswordException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UsernameNotFoundException.class})
    public Map<String, String> handleNonExistingUser(UsernameNotFoundException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({InvalidPasswordResetTokenException.class})
    public Map<String, String> handleInvalidPasswordResetToken(InvalidPasswordResetTokenException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ImageAlreadyExistsException.class})
    public Map<String, String> handleExistingImage(ImageAlreadyExistsException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ImageNotFoundException.class})
    public Map<String, String> handleNonExistingImage(ImageNotFoundException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({FileNotAnImageException.class})
    public Map<String, String> handleFileNotAnImage(FileNotAnImageException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({MultipartException.class})
    public Map<String, String> handleFileNotAnImage(MultipartException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({InvalidSortOrderException.class})
    public Map<String, String> handleInvalidSortOrder(InvalidSortOrderException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("Error", ex.getMessage());
        return errorMap;
    }
}
