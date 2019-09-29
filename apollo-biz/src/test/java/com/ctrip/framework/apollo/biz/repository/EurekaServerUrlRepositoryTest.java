package com.ctrip.framework.apollo.biz.repository;

import com.ctrip.framework.apollo.biz.BizTestConfiguration;
import com.ctrip.framework.apollo.biz.entity.ServerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.config.EurekaClientConfigServerAutoConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@EnableAutoConfiguration(exclude = {
        EurekaClientAutoConfiguration.class,
        EurekaClientConfigServerAutoConfiguration.class
})
@RunWith(SpringJUnit4ClassRunner.class)
@Rollback
@Transactional
@SpringBootTest(classes = BizTestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EurekaServerUrlRepositoryTest {

  @Autowired
  private ServerConfigRepository repository;

  private static final String KEY = "eureka.service.url";
  private static final String CLUSTER = "default";

  @Test
  public void testGetEurekaServerUrl() {

    ServerConfig config = new ServerConfig();
    config.setCluster(CLUSTER);
    config.setKey(KEY);
    config.setComment("Eureka服务Url，多个service以英文逗号分隔");
    config.setValue("http://localhost:8080/eureka/");
    config.setDataChangeCreatedBy("default");
    config.setDataChangeCreatedTime(new Date());

    ServerConfig saveConfig = repository.save(config);
    System.out.println("SaveConfig: " + saveConfig);

    config = repository.findTopByKeyAndCluster(KEY, CLUSTER);

    System.out.println("ServerConfig: " + config);

  }

}
