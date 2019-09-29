package com.ctrip.framework.apollo.autoconfigure;

import com.ctrip.framework.apollo.annotations.ApolloConfigBeanProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Arvin
 * @since 2019-09-29
 */
@Configuration
@ConditionalOnClass({RefreshScope.class, BeanPostProcessor.class, ApolloConfigBeanProcessor.class})
public class ApolloConfigBeanAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApolloConfigBeanProcessor apolloConfigBeanProcessor(Environment environment, ApplicationContext applicationContext) {
        return new ApolloConfigBeanProcessor(environment, applicationContext);
    }
}
