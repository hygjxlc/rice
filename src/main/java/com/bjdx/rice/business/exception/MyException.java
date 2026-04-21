
package com.bjdx.rice.business.exception;



/**
 * 自定义异常
 *
 * @author Mark sunlightcs@gmail.com
 */
public class MyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = ResponseCode.FAIL.code;

    public MyException(String msg) {
        super(msg);
        this.msg = msg;
    }

    /**
     * 格式化错误消息：
     * MyException("Hello %s, welcome to %s", "John", "BJDX");
     * 输出：Hello John, welcome to BJDX
     * @param msg
     * @param msgArgs
     */
    public MyException(String msg, String... msgArgs){
        this.msg = String.format(msg, msgArgs);
    }

    public MyException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public MyException(int code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public MyException(int code, String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public MyException(ResponseCode responseCode) {
        super(responseCode.msg);
        this.code = responseCode.code;
        this.msg = responseCode.msg;
    }

    public MyException(ResponseCode responseCode, Throwable e) {
        super(responseCode.msg, e);
        this.code = responseCode.code;
        this.msg = responseCode.msg;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
