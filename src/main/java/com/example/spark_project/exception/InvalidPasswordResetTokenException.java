package com.example.spark_project.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {

    private String message;

    public InvalidPasswordResetTokenException() {
    }

    public InvalidPasswordResetTokenException(String message) {
        super(message);
        this.message = message;
    }
}
