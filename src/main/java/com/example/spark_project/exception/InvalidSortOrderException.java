package com.example.spark_project.exception;

public class InvalidSortOrderException extends RuntimeException {

    private String message;

    public InvalidSortOrderException() {
    }

    public InvalidSortOrderException(String message) {
        super(message);
        this.message = message;
    }
}
