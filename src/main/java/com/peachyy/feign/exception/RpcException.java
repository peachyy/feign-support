package com.peachyy.feign.exception;

/**
 * Created  on 2018/8/15.
 *
 * @author Xs.Tao
 */
public class RpcException extends RuntimeException {


    private static final long serialVersionUID = 878955662917720571L;

    private String path;

    public String getPath() {
        return path;
    }

    public RpcException setPath(String path) {
        this.path = path;
        return this;
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
