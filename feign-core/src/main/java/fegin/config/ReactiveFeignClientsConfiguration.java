package fegin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * 默认的Feign配置类，通过ReactiveFeignNamedContextFactory自动注入
 *
 * @author liuyang
 * 创建时间: 2022-07-22 13:56
 */
public class ReactiveFeignClientsConfiguration {

    @Bean
    @Scope("prototype")
    public ReactiveFeignConfigurator reactiveFeignBasicConfigurator() {
        return new ReactiveFeignBasicConfigurator();
    }
}
