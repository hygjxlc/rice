package com.bjdx.rice.business.exception;

import com.alibaba.fastjson.JSONException;

import com.bjdx.rice.business.dto.ResponseObj;
import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * @program: 全局异常处理
 * @description
 * @author: leo
 * @create: 2021-04-19 15:33
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 请求方式不支持
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseObj handleException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("[405] 请求方法不支持: {} {}, 支持方法: {}", request.getMethod(), request.getRequestURI(), e.getSupportedMethods());
        return ResponseObj.error(ResponseCode.FAIL);
    }

    /**
     * 方法校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseObj handleException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseObj.error(message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseObj handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseObj.error(ResponseCode.FAIL);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseObj handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.getMessage(), e);
        return ResponseObj.error(ResponseCode.FAIL);
    }

    /**
     * 自定义运行时异常
     */
    @ExceptionHandler(MyException.class)
    public ResponseObj errorInfoException(MyException e) {
        log.error(e.getMessage(), e);
        return ResponseObj.error(e.getCode(), e.getMessage());
    }
    /**
     * 自定义流处理时异常
     */
    @ExceptionHandler(MyStreamException.class)
    public ResponseEntity<ResponseObj> handleInternalError(MyStreamException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseObj.error(e.getCode(), e.getMessage()));
    }
    /**
     * JSON报错
     */
    @ExceptionHandler({JsonParseException.class, HttpMessageNotReadableException.class})
    public ResponseObj jsonParseException(Exception e) {
        log.error("JSON异常:"+e.getMessage(), e);
        return ResponseObj.error(ResponseCode.FAIL.code, "JSON异常");
    }

    /**
     * json数据转换异常
     */
    @ExceptionHandler(JSONException.class)
    public ResponseObj jSONException(JSONException e) {
        log.error("传入数据类型不是期待数据类型", e);
        return ResponseObj.error(ResponseCode.FAIL.code, "传入数据类型不是期待数据类型");
    }

    /**
     * 数据类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    public ResponseObj dateTypeException(ClassCastException e) {
        log.error("数据类型转换异常", e);
        return ResponseObj.error(ResponseCode.FAIL.code, "数据类型转换异常");
    }

    /**
     * 传入数据的类型不匹配
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseObj dateTypeException(ConstraintViolationException e) {
        log.error("传入数据的类型不匹配", e);
        return ResponseObj.error(ResponseCode.FAIL.code, "传入数据的类型不匹配");
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseObj notFount(RuntimeException e) {
        log.error("运行时异常:", e);
        return ResponseObj.error(ResponseCode.FAIL.code, "运行时异常，请联系管理员");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseObj handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseObj.error(e.hashCode(), e.getMessage());
    }

    /**
     * 系统错误
     */
    @ExceptionHandler(Error.class)
    public ResponseObj handleError(Error e) {
        log.error(e.getMessage(), e);
        return ResponseObj.error(ResponseCode.FAIL.code, "服务器错误，请联系管理员");
    }


}
