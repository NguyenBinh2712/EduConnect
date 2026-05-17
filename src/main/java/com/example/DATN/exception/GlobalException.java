package com.example.DATN.exception;


import com.example.DATN.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlerRuntimeException(RuntimeException exception){

        ApiResponse apiResponse=ApiResponse.builder()
                .code(9999)
                .message(exception.getMessage())
                .build();
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlerAppException(AppException exception){
        ErrorCode errorCode=exception.getErrorCode();
        ApiResponse apiResponse=ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessages())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlerValidatorException(MethodArgumentNotValidException exception){
        String enumKey=exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode=ErrorCode.INVALID_KEY;
        try{
            errorCode=ErrorCode.valueOf(enumKey);
        }catch (IllegalArgumentException e){

        }
        ApiResponse apiResponse=ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessages())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlerAccessDeniedException(AccessDeniedException e){
        ErrorCode errorCode=ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatus()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessages())
                        .build()
        );
    }
}
