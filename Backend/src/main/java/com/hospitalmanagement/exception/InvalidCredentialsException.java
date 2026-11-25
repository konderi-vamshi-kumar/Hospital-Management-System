package com.hospitalmanagement.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Username or password is incorrect.");
    }
}
