package ru.kata.spring.boot_security.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;


import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<ExceptionInfo> handleUsernameExistException(UsernameExistException ex) {
        return new ResponseEntity<>(
                new ExceptionInfo(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionInfo> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String error = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ExceptionInfo(error),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionInfo> handleGenericException(Exception ex) {
        return new ResponseEntity<>(
                new ExceptionInfo("Произошла внутренняя ошибка"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}