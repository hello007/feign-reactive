package fegin.interceptor;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

/**
 * 注入ExchangeFilterFunction到WebClient中
 *
 * @author liuyang
 * 创建时间: 2022-07-28 16:33
 */
public interface ReactiveExchangeFilterFunction {

    /**
     * 获取注入的ExchangeFilterFunction对象
     *
     * @return ExchangeFilterFunction对象
     */
    ExchangeFilterFunction getExchangeFilterFunction();
}
