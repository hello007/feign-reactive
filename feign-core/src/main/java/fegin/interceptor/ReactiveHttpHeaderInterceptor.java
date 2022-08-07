package fegin.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * 自定义HttpHeader拦截器，方便注入自定义header值
 *
 * @author liuyang
 * 创建时间: 2022-07-24 16:17
 */
public interface ReactiveHttpHeaderInterceptor extends ReactiveHttpRequestInterceptor, Consumer<HttpHeaders> {

    /**
     * 默认实现ReactiveHttpRequestInterceptor接口，转换为Consumer方便注入header
     *
     * @param clientRequest 原ClientRequest对象
     * @return 新ClientRequest对象
     */
    @Override
    default Mono<ClientRequest> apply(ClientRequest clientRequest) {
        ClientRequest newRequest = ClientRequest.from(clientRequest).headers(this).build();
        return Mono.just(newRequest);
    }
}
