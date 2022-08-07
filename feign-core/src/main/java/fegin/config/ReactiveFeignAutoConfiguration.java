package fegin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign自动注入配置类
 * <p>
 * 1. 注入ReactiveFeignNamedContextFactory
 *
 * @author liuyang
 * 创建时间: 2022-07-23 22:36
 */
@Configuration
public class ReactiveFeignAutoConfiguration {

    @Autowired(required = false)
    private List<ReactiveFeignClientSpecification> configurations = new ArrayList<>();

    @Bean
    public ReactiveFeignNamedContextFactory reactiveFeignContext() {
        ReactiveFeignNamedContextFactory context = new ReactiveFeignNamedContextFactory();
        context.setConfigurations(this.configurations);
        return context;
    }
}
