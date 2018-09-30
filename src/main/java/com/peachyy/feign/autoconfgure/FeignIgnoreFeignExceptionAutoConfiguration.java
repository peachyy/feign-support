package com.peachyy.feign.autoconfgure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配合  配置feign.ignoreException.enable=true启用默认就是启用FeignErrorCustomDecoder
 *<br/>
 *
 * @author Xs.Tao
 */
@Configuration
@ConditionalOnClass({feign.Feign.class, feign.codec.ErrorDecoder.class})
@ConditionalOnProperty(value = "feign.ignoreException.enable",havingValue = "true")
public class FeignIgnoreFeignExceptionAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(FeignErrorCustomDecoder.class)
    public FeignErrorCustomDecoder feignErrorCustomDecoder() {

        return new FeignErrorCustomDecoder();
    }

    @Bean
    @ConditionalOnMissingBean(FeignHeaderProcessInterceptor.class)
    public FeignHeaderProcessInterceptor FeignHeaderProcessInterceptor(){
        return new FeignHeaderProcessInterceptor();
    }

}
