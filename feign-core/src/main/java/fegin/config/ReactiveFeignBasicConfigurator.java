package fegin.config;

import fegin.domain.ClientOptionDefinition;
import fegin.domain.ReactiveFeign;
import fegin.interceptor.ReactiveExchangeFilterFunction;
import fegin.interceptor.ReactiveHttpRequestInterceptor;
import fegin.interceptor.ReactiveHttpResponseInterceptor;
import fegin.retry.ReactiveRetryPolicy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 每一个feign客户端默认配置类，通过ReactiveFeignNamedContextFactory自动创建
 * 1. 获得所有的interceptor并转换为ExchangeFilterFunction
 * 2. 对应的重试策略：ReactiveRetryPolicy
 * 3. 对应的WebClient连接的配置，如连接超时时间等
 *
 * @author liuyang
 * 创建时间: 2022-07-24 13:38
 */
public class ReactiveFeignBasicConfigurator extends AbstractReactiveFeignConfigurator {

    public ReactiveFeignBasicConfigurator() {
        super(1);
    }

    @Override
    public void configure(ReactiveFeign feign, ReactiveFeignNamedContext namedContext) {
        Map<String, ReactiveHttpRequestInterceptor> requestInterceptorMap = namedContext.getAll(ReactiveHttpRequestInterceptor.class);
        List<ExchangeFilterFunction> functions = new ArrayList<>(4);
        if (!CollectionUtils.isEmpty(requestInterceptorMap)) {
            List<ReactiveHttpRequestInterceptor> interceptors = new ArrayList<>(requestInterceptorMap.values());
            AnnotationAwareOrderComparator.sort(interceptors);
            List<ExchangeFilterFunction> filterFunctions = interceptors.stream()
                    .map(ExchangeFilterFunction::ofRequestProcessor)
                    .collect(Collectors.toList());
            functions.addAll(filterFunctions);
        }
        Map<String, ReactiveHttpResponseInterceptor> responseInterceptorMap = namedContext.getAll(ReactiveHttpResponseInterceptor.class);
        if (!CollectionUtils.isEmpty(responseInterceptorMap)) {
            List<ReactiveHttpResponseInterceptor> interceptors = new ArrayList<>(responseInterceptorMap.values());
            AnnotationAwareOrderComparator.sort(interceptors);
            List<ExchangeFilterFunction> filterFunctions = interceptors.stream()
                    .map(ExchangeFilterFunction::ofResponseProcessor)
                    .collect(Collectors.toList());
            functions.addAll(filterFunctions);
        }
        Map<String, ReactiveExchangeFilterFunction> allFilterFunctions = namedContext.getAll(ReactiveExchangeFilterFunction.class);
        if (!CollectionUtils.isEmpty(allFilterFunctions)) {
            allFilterFunctions.values().forEach(function -> functions.add(function.getExchangeFilterFunction()));
        }
        Optional<ExchangeFilterFunction> exchangeFilterFunction = functions.stream()
                .distinct().filter(Objects::nonNull)
                .reduce(ExchangeFilterFunction::andThen);
        exchangeFilterFunction.ifPresent(feign::setFilterFunction);

        ReactiveRetryPolicy retryPolicy = namedContext.getOptional(ReactiveRetryPolicy.class);
        feign.setRetryPolicy(retryPolicy);

        ClientOptionDefinition clientOption = namedContext.getOptional(ClientOptionDefinition.class);
        feign.setOptionDefinition(clientOption);
    }
}
