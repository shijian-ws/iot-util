package org.sj.iot.exception;

/**
 * 数据访问异常
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-10-18
 */
public class DataAccessException extends RuntimeException {
    private int code;
    private String[] args;

    public DataAccessException() {
    }

    public DataAccessException(int code) {
        this.code = code;
    }

    public DataAccessException(int code, String... args) {
        this.code = code;
        this.args = args;
    }

    public DataAccessException(String message, int code) {
        super(message);
        this.code = code;
    }

    public DataAccessException(String message, int code, String... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public DataAccessException(Throwable cause, int code) {
        super(cause.getMessage(), cause);
        this.code = code;
    }

    public DataAccessException(Throwable cause, int code, String... args) {
        super(cause.getMessage(), cause);
        this.code = code;
        this.args = args;
    }

    public DataAccessException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public DataAccessException(String message, Throwable cause, int code, String... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}
