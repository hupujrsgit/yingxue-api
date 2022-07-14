package com.exeplm.exception;

import lombok.Data;
import org.springframework.stereotype.Component;


@Data
public class UserException extends RuntimeException{


    public UserException(String message) {
        super(message);
    }
}
