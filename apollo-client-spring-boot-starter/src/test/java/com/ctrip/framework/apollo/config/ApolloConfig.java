package com.ctrip.framework.apollo.config;

import com.ctrip.framework.apollo.annotations.ApolloConfigBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;
import java.util.Map;

/**
 * @author Arvin
 * @since 2019-06-27
 */
@ConfigurationProperties(prefix = "apollo.config")
@ApolloConfigBean
@RefreshScope
public class ApolloConfig {

    private int expireSeconds;
    private String clusterNodes;
    private int commandTimeout;

    private Map<String, String> someMap;
    private List<String> someList;

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public int getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(int commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public Map<String, String> getSomeMap() {
        return someMap;
    }

    public void setSomeMap(Map<String, String> someMap) {
        this.someMap = someMap;
    }

    public List<String> getSomeList() {
        return someList;
    }

    public void setSomeList(List<String> someList) {
        this.someList = someList;
    }

    @Override
    public String toString() {
        return "ApolloConfig{" +
                "expireSeconds=" + expireSeconds +
                ", clusterNodes='" + clusterNodes + '\'' +
                ", commandTimeout=" + commandTimeout +
                ", someMap=" + someMap +
                ", someList=" + someList +
                '}';
    }
}
