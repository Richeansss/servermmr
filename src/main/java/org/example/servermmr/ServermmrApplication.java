package org.example.servermmr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServermmrApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServermmrApplication.class, args);
    }

}
