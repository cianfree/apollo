package com.ctrip.framework.apollo.annotations;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author Arvin
 * @since 2019-09-29
 */
public abstract class AbstractConfigRefreshHandler implements ConfigRefreshHandler {

    @Override
    public void beforeRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {

    }

    @Override
    public void afterRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean) {

    }

    @Override
    public int getHandlerOrder() {
        return 0;
    }
}
