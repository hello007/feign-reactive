package fegin.config;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * 自定义ReactiveFeignClientSpecification使用的NamedContextFactory
 * <p>
 * 默认配置类为ReactiveFeignClientsConfiguration
 *
 * @author liuyang
 * 创建时间: 2022-07-22 11:35
 */
public class ReactiveFeignNamedContextFactory extends NamedContextFactory<ReactiveFeignClientSpecification> {
    public ReactiveFeignNamedContextFactory() {
        super(ReactiveFeignClientsConfiguration.class, "reactive-feign-config", "reactive.feign.client.name");
    }
}
