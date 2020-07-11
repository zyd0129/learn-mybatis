package com.learn.authserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;

@SpringBootApplication
@MapperScan("com.learn.authserver.dao.mapper")
public class AuthServerApplication  implements CommandLineRunner {

    @Autowired
    KeyProperties keyProperties;

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(keyProperties.getKeyStore());
    }
}
