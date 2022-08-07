package fegin.domain;

import fegin.fallback.FallbackFactory;
import fegin.handler.DefaultMethodHandler;
import fegin.handler.FallbackMethodHandler;
import fegin.handler.MethodHandler;
import fegin.handler.ReactiveInvocationHandler;
import fegin.handler.ReactiveMethodHandler;
import fegin.handler.RetryMethodHandler;
import fegin.retry.ReactiveRetryPolicy;
import fegin.utils.AnnotationUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ReactiveFeign对象，构造每个方法对应的MethodHandler
 *
 * @author liuyang
 * 创建时间: 2022-07-23 16:30
 */
public class ReactiveFeign {

    private final Target target;

    private final List<MethodMetadata> methodMetadata;

    private ExchangeFilterFunction filterFunction;

    private ReactiveRetryPolicy retryPolicy;

    private FallbackFactory fallbackFactory;

    private ClientOptionDefinition optionDefinition;

    public ReactiveFeign(Target target, List<MethodMetadata> methodMetadata) {
        this.target = target;
        this.methodMetadata = methodMetadata;
    }

    public <T> T newInstance() {
        Map<Method, MethodHandler> methodToHandler = transfer();

        List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<>();

        for (Method method : target.getTargetType().getMethods()) {
            if (methodToHandler.containsKey(method) || method.getDeclaringClass() == Object.class) {
                continue;
            } else if (AnnotationUtils.isDefault(method)) {
                DefaultMethodHandler handler = new DefaultMethodHandler(method);
                defaultMethodHandlers.add(handler);
                methodToHandler.put(method, handler);
            }
        }

        ReactiveInvocationHandler handler = new ReactiveInvocationHandler(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(target.getTargetType().getClassLoader(),
                new Class<?>[]{target.getTargetType()}, handler);

        for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
            defaultMethodHandler.bindTo(proxy);
        }

        return proxy;
    }

    /**
     * 将MethodMetadata对象转换为MethodHandler对象
     *
     * @return 所有接口内非default方法对应的MethodHandler
     */
    private Map<Method, MethodHandler> transfer() {
        Map<Method, MethodHandler> nameToHandler = new LinkedHashMap<>();
        for (MethodMetadata md : methodMetadata) {
            MethodHandler handler = getMethodHandler(md);
            nameToHandler.put(md.getMethod(), handler);
        }
        return nameToHandler;
    }

    private MethodHandler getMethodHandler(MethodMetadata md) {
        MethodHandler handler = new ReactiveMethodHandler(md);
        if (filterFunction != null) {
            ((ReactiveMethodHandler) handler).setFilterFunction(filterFunction);
        }
        if (optionDefinition != null) {
            ((ReactiveMethodHandler) handler).setOptionDefinition(optionDefinition);
        }
        if (retryPolicy != null && retryPolicy.retry(md) != null) {
            handler = new RetryMethodHandler(md, handler, retryPolicy.retry(md));
        }
        if (fallbackFactory != null) {
            handler = new FallbackMethodHandler(md, handler, fallbackFactory);
        }
        return handler;
    }

    public ExchangeFilterFunction getFilterFunction() {
        return filterFunction;
    }

    public void setFilterFunction(ExchangeFilterFunction filterFunction) {
        this.filterFunction = filterFunction;
    }

    public ReactiveRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(ReactiveRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public FallbackFactory getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(FallbackFactory fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }

    public ClientOptionDefinition getOptionDefinition() {
        return optionDefinition;
    }

    public void setOptionDefinition(ClientOptionDefinition optionDefinition) {
        this.optionDefinition = optionDefinition;
    }
}
