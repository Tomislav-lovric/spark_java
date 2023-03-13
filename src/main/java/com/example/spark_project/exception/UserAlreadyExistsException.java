package com.example.spark_project.exception;

public class UserAlreadyExistsException extends RuntimeException {

    private String message;

    public UserAlreadyExistsException() {
    }

    public UserAlreadyExistsException(String message) {
        super(message);
        this.message = message;
    }
}
