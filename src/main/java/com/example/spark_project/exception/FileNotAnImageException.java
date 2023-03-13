package com.example.spark_project.exception;

public class FileNotAnImageException extends RuntimeException {

    private String message;

    public FileNotAnImageException() {
    }

    public FileNotAnImageException(String message) {
        super(message);
        this.message = message;
    }
}
