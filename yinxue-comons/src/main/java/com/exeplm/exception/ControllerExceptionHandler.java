package com.exeplm.exception;

import com.exeplm.utils.TaoResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自定义异常类
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(UserException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<TaoResult> handlerUserNotExistException(UserException ex){

        return new ResponseEntity<>(TaoResult.build(500,ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
