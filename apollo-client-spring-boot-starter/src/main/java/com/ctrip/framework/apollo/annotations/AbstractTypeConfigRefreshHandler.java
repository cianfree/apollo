package com.ctrip.framework.apollo.annotations;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import java.lang.reflect.ParameterizedType;

/**
 * @author Arvin
 * @since 2019-09-29
 */
public abstract class AbstractTypeConfigRefreshHandler<T> extends AbstractConfigRefreshHandler {

    private Class<T> configBeanType;

    private String matchBeanName;

    public AbstractTypeConfigRefreshHandler() {
        this(null);
    }

    public AbstractTypeConfigRefreshHandler(String matchBeanName) {
        ParameterizedType type = (ParameterizedType) this.getClass()
                .getGenericSuperclass();
        this.configBeanType = (Class<T>) type.getActualTypeArguments()[0];
        this.matchBeanName = matchBeanName;
    }

    @Override
    public boolean accept(ConfigChangeEvent changeEvent, String beanName, Object bean) {
        boolean isInstance = configBeanType.isInstance(bean) || bean.getClass() == configBeanType;
        if (isInstance) {
            boolean isBeanNameMatch = true;
            if (null != matchBeanName && !"".equals(matchBeanName.trim())) {
                isBeanNameMatch = matchBeanName.equals(beanName);
            }

            return isBeanNameMatch;
        }
        return false;
    }
}
