package com.ctrip.framework.apollo.annotations;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author Arvin
 * @since 2019-09-29
 */
public interface ConfigRefreshHandler {

    boolean accept(ConfigChangeEvent changeEvent, String beanName, Object bean);

    void beforeRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean);

    void afterRefresh(ConfigChangeEvent changeEvent, String beanName, Object bean);

    /**
     * 排序，越小 就越先执行
     *
     * @return 排序
     */
    int getHandlerOrder();
}
