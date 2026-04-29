package com.niladri.productservice.exception;

public class CategoryAlreadyExists extends RuntimeException{
    public CategoryAlreadyExists(String message){
        super(message);
    }
}
