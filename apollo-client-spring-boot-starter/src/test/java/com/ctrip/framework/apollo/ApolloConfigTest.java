package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.config.ApolloConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Arvin
 * @since 2019-06-28
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApolloConfigTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@SpringBootApplication
public class ApolloConfigTest {

    static {
        // 指定到开发环境
        System.setProperty("dev_meta", "http://localhost:8081");
    }

    public static void main(String[] args) {
        SpringApplication.run(ApolloConfigTest.class, args);
    }

    @Value("${timeout}")
    private long timeout;

    @Value("${dev1.name}")
    private String devName;

    @Autowired
    private ApolloConfig apolloConfig;

    @Autowired
    private RefreshScope refreshScope;

    @Test
    public void testApolloConfig() throws InterruptedException {
        while (true) {

            System.out.println("=====================================================================================");

            System.out.println("Timeout: " + timeout);
            System.out.println("DevName: " + devName);
            System.out.println("ApolloConfig: " + apolloConfig);

            Thread.sleep(5000);
        }
    }

}
