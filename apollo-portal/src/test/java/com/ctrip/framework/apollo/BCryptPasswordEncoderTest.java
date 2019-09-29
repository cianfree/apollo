package com.ctrip.framework.apollo;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Arvin
 * @since 2019-06-28
 */
public class BCryptPasswordEncoderTest {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    public void testPasswordEncode() {

        System.out.println(encoder.encode("admin"));
        System.out.println(encoder.encode("apollo"));
        System.out.println(encoder.encode("zxadmin123"));
        System.out.println(encoder.encode("123456"));

    }
}
