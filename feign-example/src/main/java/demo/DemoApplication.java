package demo;

import fegin.annotations.EnableReactiveFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:32
 */
@SpringBootApplication
@EnableReactiveFeignClients
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
