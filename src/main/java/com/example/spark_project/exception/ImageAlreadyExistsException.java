package com.example.spark_project.exception;

public class ImageAlreadyExistsException extends RuntimeException {

    private String message;

    public ImageAlreadyExistsException() {
    }

    public ImageAlreadyExistsException(String message) {
        super(message);
        this.message = message;
    }
}
