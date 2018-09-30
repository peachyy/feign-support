package com.peachyy.feign.autoconfgure;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 支持在非人为主动请求的条件下转发覆盖初次请求的header
 * 为了解决在远程RPC中获取当前登录人的相关数据
 *
 * @author Xs.Tao
 */
public class FeignHeaderProcessInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            HttpServletRequest request = getRequest();
            if (request != null) {
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name   = headerNames.nextElement();
                        String values = request.getHeader(name);
                        requestTemplate.header(name, values);
                    }
                }
            }
        } catch (Throwable e) {
            //skip
        }
    }

    private HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }
}
