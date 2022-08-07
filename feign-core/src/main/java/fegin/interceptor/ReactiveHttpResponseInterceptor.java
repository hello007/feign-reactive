package fegin.interceptor;

import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Response拦截器
 *
 * @author liuyang
 * 创建时间: 2022-07-24 16:16
 */
public interface ReactiveHttpResponseInterceptor extends Function<ClientResponse, Mono<ClientResponse>>, Ordered {

    /**
     * 优先级，排序使用
     *
     * @return 优先级
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
