package com.ctrip.framework.apollo.annotations;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * @author Arvin
 * @since 2019-09-29
 */
public class RefreshConfigChangeListener implements ConfigChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationContext applicationContext;

    private final String beanName;

    private RefreshScope refreshScope;

    public RefreshConfigChangeListener(ApplicationContext applicationContext, String beanName) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {

        Object beforeRefreshBean = applicationContext.getBean(beanName);

        List<ConfigRefreshHandler> handlers = lookupConfigRefreshHandler(changeEvent, beanName, beforeRefreshBean);

        // 刷新属性
        doRefreshBefore(handlers, changeEvent, beforeRefreshBean);
        logger.info("Before Refresh Apollo Config Bean: name={}, bean={}", beanName, beforeRefreshBean);

        RefreshScope refreshScope = getRefreshScope();
        refreshScope.refresh(beanName);

        Object afterRefreshBean = applicationContext.getBean(beanName);
        doRefreshAfter(handlers, changeEvent, afterRefreshBean);
        logger.info("After Refresh Apollo Config Bean: name={}, bean={}", beanName, afterRefreshBean);
    }

    private RefreshScope getRefreshScope() {

        if (null != refreshScope) {
            return refreshScope;
        }
        refreshScope = applicationContext.getBean(RefreshScope.class);
        return refreshScope;

    }

    private void doRefreshAfter(List<ConfigRefreshHandler> handlers, ConfigChangeEvent changeEvent, Object bean) {
        if (handlers != null && !handlers.isEmpty()) {
            try {
                for (ConfigRefreshHandler handler : handlers) {
                    handler.afterRefresh(changeEvent, beanName, bean);
                }
            } catch (Exception e) {
                logger.warn("执行后置ApolloConfigBean配置变更通知失败，error={}", e.getMessage(), e);
            }
        }
    }

    private void doRefreshBefore(List<ConfigRefreshHandler> handlers, ConfigChangeEvent changeEvent, Object bean) {

        if (handlers != null && !handlers.isEmpty()) {
            try {
                for (ConfigRefreshHandler handler : handlers) {
                    handler.beforeRefresh(changeEvent, beanName, bean);
                }
            } catch (Exception e) {
                logger.warn("执行前置ApolloConfigBean配置变更通知失败，error={}", e.getMessage(), e);
            }
        }

    }

    private List<ConfigRefreshHandler> handlers = null;

    private List<ConfigRefreshHandler> lookupConfigRefreshHandler(ConfigChangeEvent changeEvent, String beanName, Object bean) {
        if (null == handlers) {
            List<ConfigRefreshHandler> handlers = new ArrayList<>();
            try {
                Map<String, ? extends ConfigRefreshHandler> map = applicationContext.getBeansOfType(ConfigRefreshHandler.class);
                if (null != map && !map.isEmpty()) {

                    for (Map.Entry<String, ? extends ConfigRefreshHandler> entry : map.entrySet()) {
                        ConfigRefreshHandler handler = entry.getValue();
                        if (handler.accept(changeEvent, beanName, bean)) {
                            handlers.add(handler);
                        }
                    }
                }
            } catch (BeansException e) {
                // ignored
            }

            // 排序, handlerOrder 越小，优先级越高
            Collections.sort(handlers, new Comparator<ConfigRefreshHandler>() {
                @Override
                public int compare(ConfigRefreshHandler o1, ConfigRefreshHandler o2) {
                    return Integer.compare(o1.getHandlerOrder(), o2.getHandlerOrder());
                }
            });

            this.handlers = handlers;
        }
        return handlers;
    }
}
