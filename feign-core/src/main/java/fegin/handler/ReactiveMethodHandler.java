package fegin.handler;

import fegin.domain.ClientOption;
import fegin.domain.ClientOptionDefinition;
import fegin.domain.MethodMetadata;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * WebClient接口调用处理类
 * <p>
 * 1. 构造并配置HttpClient
 * 2. 构造WebClient
 * 3. ClientResponse对象转换为Mono对象
 *
 * @author liuyang
 * 创建时间: 2022-07-23 21:41
 */
public class ReactiveMethodHandler implements MethodHandler {

    private final MethodMetadata metadata;

    private ExchangeFilterFunction filterFunction;

    private ClientOptionDefinition optionDefinition;

    public ReactiveMethodHandler(MethodMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Object[] args) {
        // 构造parameter map
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        metadata.getParamIndexNameMap().forEach((key, v) -> paramsMap.add(v, args[key] == null ? null : args[key].toString()));

        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        metadata.getHeaderIndexNameMap().forEach((k, v) -> headerMap.add(v, args[k] == null ? null : args[k].toString()));

        // 构造buildValue map，用于替换 {name} 格式的uri
        Map<String, Object> buildValue = new LinkedHashMap<>();
        metadata.getVariableIndexNameMap().forEach((key, value) -> buildValue.put(value, args[key]));

        Builder builder = WebClient.builder().clientConnector(new ReactorClientHttpConnector(getAndConfigClient()));
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);
        String uri = metadata.getPrefixUrl() + metadata.getPath();
        String url = uriFactory.uriString(uri).queryParams(paramsMap).build(buildValue).toASCIIString();

        if (filterFunction != null) {
            builder.filter(filterFunction);
        }

        RequestBodyUriSpec spec = builder.baseUrl(url)
                .defaultHeaders(headers -> {
                    headers.putAll(metadata.getHeaders());
                    headers.putAll(headerMap);
                })
                .build().method(metadata.getHttpMethod());

        Publisher<?> mono;
        if (metadata.getBodyIndex() > -1) {
            mono = spec.bodyValue(args[metadata.getBodyIndex()])
                    .exchange().flatMap(this::transferResponse);
        } else {
            mono = spec.exchange().flatMap(this::transferResponse);
        }
        if (metadata.isFlux()) {
            return Flux.from(mono);
        }
        return mono;
    }

    /**
     * 将ClientResponse对象转换为定义的returnType对象
     *
     * @param response 响应结果
     * @return 转换后的Mono结果
     */
    public Mono<?> transferResponse(ClientResponse response) {
        ParameterizedTypeReference<?> typeReference = ParameterizedTypeReference.forType(metadata.getReturnType());
        if (metadata.isResponseEntity()) {
            return response.toEntity(typeReference);
        } else {
            return response.bodyToMono(typeReference);
        }
    }

    /**
     * 获取并配置HttpClient对象
     *
     * @return 配置完的HttpClient对象
     */
    private HttpClient getAndConfigClient() {
        int readTimeout = -1;
        int writeTimeout = -1;
        UnaryOperator<TcpClient> mapperFunction = null;
        if (optionDefinition != null && optionDefinition.get() != null) {
            List<ClientOption<?>> clientOptions = new ArrayList<>(optionDefinition.get());
            Iterator<ClientOption<?>> iterator = clientOptions.iterator();
            while (iterator.hasNext()) {
                ClientOption<?> next = iterator.next();
                if (ClientOption.READ_TIMEOUT_OPTION.equals(next.getChannelOption())) {
                    readTimeout = (Integer) next.getT();
                    iterator.remove();
                } else if (ClientOption.WRITE_TIMEOUT_OPTION.equals(next.getChannelOption())) {
                    iterator.remove();
                    writeTimeout = (Integer) next.getT();
                }
            }
            int finalReadTimeout = readTimeout;
            int finalWriteTimeout = writeTimeout;
            mapperFunction = client -> {
                TcpClient newClient = client;
                for (ClientOption option : clientOptions) {
                    newClient = newClient.option(option.getChannelOption(), option.getT());
                }
                newClient = newClient.doOnConnected(connection -> {
                    if (finalReadTimeout > 0) {
                        connection.addHandler(new ReadTimeoutHandler(finalReadTimeout, TimeUnit.MILLISECONDS));
                    }
                    if (finalWriteTimeout > 0) {
                        connection.addHandler(new WriteTimeoutHandler(finalWriteTimeout, TimeUnit.MILLISECONDS));
                    }
                });
                return newClient;
            };
        }

        HttpClient httpClient = HttpClient.create();
        if (mapperFunction != null) {
            httpClient = httpClient.tcpConfiguration(mapperFunction);
        }
        return httpClient;
    }

    public void setFilterFunction(ExchangeFilterFunction filterFunction) {
        this.filterFunction = filterFunction;
    }

    public void setOptionDefinition(ClientOptionDefinition optionDefinition) {
        this.optionDefinition = optionDefinition;
    }
}
