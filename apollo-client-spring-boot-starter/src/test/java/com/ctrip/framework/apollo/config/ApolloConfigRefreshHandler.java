package com.ctrip.framework.apollo.config;

import com.ctrip.framework.apollo.annotations.AbstractTypeConfigRefreshHandler;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.springframework.stereotype.Component;

/**
 * @author Arvin
 * @since 2019-07-03
 */
@Component
public class ApolloConfigRefreshHandler extends AbstractTypeConfigRefreshHandler<ApolloConfig> {

    @Override
    public void beforeRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {
        System.err.println("Bean 配置变更了 Before");
    }

    @Override
    public void afterRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {
        System.err.println("Bean 配置变更了 After");
    }
}
