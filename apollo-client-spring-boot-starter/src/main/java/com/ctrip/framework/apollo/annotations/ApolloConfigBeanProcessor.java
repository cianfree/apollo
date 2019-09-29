package com.ctrip.framework.apollo.annotations;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 自动刷新 Apollo 配置Bean
 *
 * @author Arvin
 * @since 2019-09-29
 */
public class ApolloConfigBeanProcessor implements BeanPostProcessor, PriorityOrdered {

    private static Set<Class<?>> handledClass = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 启动的命名空间
     **/
    private String[] bootstrapNamespaces;

    private ApplicationContext applicationContext;

    public ApolloConfigBeanProcessor(Environment environment, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initBootstrapNamespaces(environment);
    }

    private void initBootstrapNamespaces(Environment environment) {
        String namespaceValue = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);

        bootstrapNamespaces = namespaceValue.split(",");
        logger.info("[{}] is config as {}", PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, bootstrapNamespaces);
    }

    private String[] adapterNamespaces(String[] value, String[] bootstrapNamespaces) {

        List<String> values = new ArrayList<>();
        for (String val : value) {
            if (null != val && !"".equals(val.trim())) {
                values.add(val);
            }
        }

        if (values.isEmpty()) {
            return bootstrapNamespaces;
        }
        return values.toArray(new String[]{});


    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        if (ClassUtils.isCglibProxy(bean)) {
            return bean;
        }

        if (handledClass.contains(clazz)) {
            return bean;
        }

        ApolloConfigBean annotation = AnnotationUtils.findAnnotation(clazz, ApolloConfigBean.class);
        if (annotation == null) {
            return bean;
        }

        ConfigurationProperties configurationProperties = AnnotationUtils.findAnnotation(clazz, ConfigurationProperties.class);
        Preconditions.checkArgument(configurationProperties != null,
                "Invalid apollo config bean type: %s need annotation ConfigurationProperties!", clazz);

        // 记录为已处理
        handledClass.add(clazz);

        String[] namespaces = adapterNamespaces(annotation.value(), bootstrapNamespaces);

        ConfigChangeListener configChangeListener = new RefreshConfigChangeListener(applicationContext, beanName);

        String prefix = configurationProperties.prefix();
        Set<String> interestedKeyPrefixes = null;
        if (!"".equals(prefix.trim())) {
            interestedKeyPrefixes = new HashSet<>(1);
            if (!prefix.endsWith(".")) {
                prefix = prefix + ".";
            }
            interestedKeyPrefixes.add(prefix);
        }

        for (String namespace : namespaces) {
            Config config = ConfigService.getConfig(namespace);

            if (interestedKeyPrefixes == null) {
                config.addChangeListener(configChangeListener);
            } else {
                logger.info("注册Apollo监听器，namespace={},ConfigChangeListener={}", namespace, configChangeListener);
                config.addChangeListener(configChangeListener, null, interestedKeyPrefixes);
            }
        }
        return bean;
    }
}
