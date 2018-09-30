package com.peachyy.feign.exception;

import com.google.common.collect.Maps;

import com.peachyy.feign.util.FastJsons;

import java.util.Map;


public class BusinessException extends RuntimeException{


	protected long   code = 500;
	protected String path;

	private StackTraceElement orignStackTrace;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
	    super(message);
	}

	public BusinessException(long code) {
		this.code = code;
	}

	public BusinessException(String message, long code) {
		super(message);
		this.code = code;
	}

	public long getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		Map<String, Object> param = Maps.newHashMap();
		param.put("code", this.code);
		param.put("msg", super.getMessage());
		return FastJsons.convertObjectToJSON(param);
	}

	public String getOriginMessage() {
		return super.getMessage();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void getOrignStackTrace(StackTraceElement orignStackTrace) {
		this.orignStackTrace = orignStackTrace;
	}

	public void setOrignStackTrace(StackTraceElement orignStackTrace) {
		this.orignStackTrace = orignStackTrace;
	}

	public StackTraceElement getOrignStackTrace() {
		return orignStackTrace;
	}
	
}
