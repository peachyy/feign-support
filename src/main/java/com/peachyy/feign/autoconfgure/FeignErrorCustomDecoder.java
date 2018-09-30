package com.peachyy.feign.autoconfgure;

import com.google.common.collect.Maps;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.peachyy.feign.exception.RpcException;
import com.peachyy.feign.util.FastJsons;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import feign.Response;
import feign.Util;


public class FeignErrorCustomDecoder implements feign.codec.ErrorDecoder {
	private static final Logger logger = LoggerFactory.getLogger(FeignErrorCustomDecoder.class);
	public static final String SUPPORT_INSIDE_EXCEPTION_NAME = "com.peachyy.feign.exception.BusinessException";

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.status() >= 400 && response.status() <= 500) {
			String body = getExceptionBody(response);
			Throwable origin = null;
			try {
				origin = newOriginException(body);
			} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
				logger.info(e.getMessage(), e);
			}
			if (origin == null) {
				logger.warn("未处理到异常{}", body);
			}
			return new HystrixBadRequestException("RPC调用失败", origin == null ? new RpcException(body) : new RpcException(body, origin));
		}
		return feign.FeignException.errorStatus(methodKey, response);
	}

	private String getExceptionBody(Response response) {
		String body = null;
		try {
			body = Util.toString(response.body().asReader());
		} catch (IOException e) {
		}
		return body;
	}

	@SuppressWarnings("unchecked")
	private Throwable newOriginException(String body) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		Map<String, Object> errorMap = FastJsons.convertJSONMap(body);
		String className = MapUtils.getString(errorMap, "exception");
		String originMessage = MapUtils.getString(errorMap, "message");
		String path = MapUtils.getString(errorMap, "path");
		Integer status = MapUtils.getInteger(errorMap, "status");

		if (StringUtils.isBlank(className)) {// 为Null得情况 可能是404或者其他
			if (status == 404) {
				className = SUPPORT_INSIDE_EXCEPTION_NAME;
				Map<String, Object> r = Maps.newHashMap();
				r.put("code", status);
				r.put("msg", MapUtils.getString(errorMap, "message"));
				r.put("path", path);
				originMessage = className.concat(": ").concat(FastJsons.convertObjectToJSON(r));
			}
		}
		if (org.springframework.util.ClassUtils.isPresent(className, org.springframework.util.ClassUtils.getDefaultClassLoader())
		// && SUPPORT_EXCEPTION_NAME.equalsIgnoreCase(className)
		) {
			Class<Exception> clazz_ = (Class<Exception>) ClassUtils.getClass(className);
			Throwable e = null;
			String message = getRealMessage(className, originMessage);
			if (buessinException(clazz_) != null && hasJSON(message)) {// just businessException.class
				Map<String, Object> map = FastJsons.convertJSONMap(message);
				Integer code = MapUtils.getInteger(map, "code", 500);
				String msg = MapUtils.getString(map, "msg", StringUtils.EMPTY);
				e = ConstructorUtils.invokeConstructor(clazz_, msg, code);
				Method method = ReflectionUtils.findMethod(clazz_, "setPath", String.class);
				if (method != null) {
					method.invoke(e, path);
				}
			} else if (runtimeException(clazz_) != null) {// accpet runtime Exception
				e = ConstructorUtils.invokeConstructor(clazz_, StringUtils.isBlank(message) ? Objects.toString(originMessage, StringUtils.EMPTY) : Objects.toString(message, StringUtils.EMPTY));
			}
			return e;
		}
		return null;
	}

	private Constructor<Exception> runtimeException(Class<Exception> clazz_) {
		if (clazz_ != null) {
			final Constructor<Exception> ctor = ConstructorUtils.getMatchingAccessibleConstructor(clazz_, String.class);
			return ctor;
		}
		return null;

	}

	private Constructor<Exception> buessinException(Class<Exception> exceptionClass) {
		if (exceptionClass.getName().equalsIgnoreCase(SUPPORT_INSIDE_EXCEPTION_NAME)) {
			final Constructor<Exception> ctor = ConstructorUtils.getMatchingAccessibleConstructor(exceptionClass, String.class, Integer.class);
			return ctor;
		}
		return null;
	}

	private boolean hasJSON(String message) {
		if (StringUtils.startsWith(message, "{")) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private String formatMessage(String className, String message) {
		if (hasJSON(message)) {
			return message;
		}
		return getRealMessage(className, message);
	}

	private String getRealMessage(String className, String message) {
		String tag = className.concat(": ");
		return StringUtils.substringAfter(message, tag);
	}

}
