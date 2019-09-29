package com.ctrip.framework.apollo.metaservice.service;

import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import com.ctrip.framework.apollo.biz.repository.ServerConfigRepository;
import com.ctrip.framework.apollo.biz.service.BizDBPropertySource;
import com.ctrip.framework.apollo.core.ConfigConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 *     自动刷新 数据库中的 ServerConfig: eureka.service.url, 逻辑如下：
 *     1. 项目启动后，先删除数据库中的，然后把自己的IP设置进去
 *     2. 定时任务，启动五分钟内，每隔1秒执行一次，五分钟后每隔15秒执行一次
 *        逻辑为：
 *          > 读取 eureka.service.url 然后追加自己的IP，然后更新到数据库
 * </pre>
 *
 * @author Arvin
 * @since 2019-09-27
 */
@Service
public class AutoRefreshEurekaUrlService implements InitializingBean, Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.cloud.client.ipAddress:${spring.cloud.client.ip-address}}")
    private String localIp;

    @Value("${server.port}")
    private int serverPort;

    @Value("${eureka.url.rebuild:true}")
    private boolean eurekaUrlRebuild;

    @Autowired
    private BizDBPropertySource bizDBPropertySource;

    private Method refreshMethod;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    private String localEurekaUrl;

    private long startTime = System.currentTimeMillis();

    private ExecutorService executorService;

    @Override
    public void afterPropertiesSet() throws Exception {

        this.localEurekaUrl = "http://" + localIp + ":" + serverPort + "/eureka/";

        if (eurekaUrlRebuild) {
            logger.info("Rebuild Eureka Url For Current ConfigService : {}:{}", localIp, serverPort);

            ServerConfig config = serverConfigRepository.findTopByKeyAndCluster(ConfigConsts.KEY_EUREKA_SERVICE_URL, ConfigConsts.CLUSTER_NAME_DEFAULT);
            config.setValue(this.localEurekaUrl);
            serverConfigRepository.save(config);

            logger.info("Update ServerConfig: {}", config);

            this.refreshMethod = bizDBPropertySource.getClass().getDeclaredMethod("refresh");
            this.refreshMethod.setAccessible(true);

            refreshBizDBPropertySource();

            scheduleEurekaUrlRebuild();
        }
    }

    private void refreshBizDBPropertySource() {
        try {
            this.refreshMethod.invoke(this.bizDBPropertySource);
        } catch (Exception e) {
            logger.warn("更新 BizDBPropertySource " + ConfigConsts.KEY_EUREKA_SERVICE_URL + " 失败，error: " + e.getMessage(), e);
        }
    }

    private void scheduleEurekaUrlRebuild() {
        this.executorService = Executors.newFixedThreadPool(1);
        this.executorService.submit(this);
    }

    /**
     * 最新的EurekaUrl
     **/
    private String lastestEurekaUrl;

    @Override
    public void run() {
        while (true) {
            try {
                sleepTask();

                ServerConfig config = serverConfigRepository.findTopByKeyAndCluster(ConfigConsts.KEY_EUREKA_SERVICE_URL, ConfigConsts.CLUSTER_NAME_DEFAULT);
                // 与本地的合并
                String newEurekaUrl = mergeEurekaUrl(config.getValue());
                config.setValue(newEurekaUrl);

                if (lastestEurekaUrl == null || !lastestEurekaUrl.equals(newEurekaUrl)) {
                    serverConfigRepository.save(config);
                    logger.info("Update ServerConfig: {}", config);

                    refreshBizDBPropertySource();
                }

                this.lastestEurekaUrl = newEurekaUrl;
            } catch (Exception e) {
                logger.warn("Update Eureka DB config failed: " + e.getMessage(), e);
            }

        }
    }

    private String mergeEurekaUrl(String value) {

        if (value == null || "".equals(value.trim())) {
            return this.localEurekaUrl;
        }

        Set<String> ipPortSet = new HashSet<>();
        ipPortSet.add(this.localIp + ":" + serverPort);

        String[] urls = value.split(",");
        for (String url : urls) {
            URI uri = URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort();
            ipPortSet.add(host + ":" + port);
        }

        StringBuilder builder = new StringBuilder();
        for (String ipPort : ipPortSet) {
            builder.append("http://").append(ipPort).append("/eureka/,");
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();

    }

    private void sleepTask() {
        try {
            if (System.currentTimeMillis() - startTime < 600000) {
                // 10分钟内，每隔5秒扫描一次
                Thread.sleep(5000);
            } else {
                // 10分钟外，每隔30秒扫描一次
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
