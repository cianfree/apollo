package com.ctrip.framework.apollo.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Arvin
 * @since 2019-09-29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ApolloConfigBean {

    /**
     * Apollo namespace for the config, if not specified then default to application
     */
    String[] value() default {};

}
