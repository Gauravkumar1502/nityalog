package dev.gaurav.nityalog;

import dev.gaurav.nityalog.properties.CorsProperties;
import dev.gaurav.nityalog.properties.JwtProperties;
import dev.gaurav.nityalog.properties.ServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({
        CorsProperties.class,
        JwtProperties.class,
        ServerProperties.class
})
public class NityaLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(NityaLogApplication.class, args);
    }

}
