package demo.client;

import fegin.retry.ReactiveRetryPolicy;
import org.springframework.context.annotation.Bean;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:41
 */
public class UserConfig {

    public static final String QUERY_USER_INFO = "queryUserInfo";

    /**
     * 注册retry策略
     */
    @Bean
    public ReactiveRetryPolicy reactiveRetryPolicy() {
        return md -> {
            String name = md.getMethod().getName();
            if (QUERY_USER_INFO.equals(name)) {
                return Retry.fixedDelay(5, Duration.ofMillis(500));
            }
            return Retry.fixedDelay(3, Duration.ofSeconds(1));
        };
    }

    /**
     * 注册特定的fallback
     */
    @Bean
    public UserClientFallback userClientFallback() {
        return new UserClientFallback();
    }
}
