package fegin.handler;

import fegin.domain.MethodMetadata;
import fegin.fallback.FallbackFactory;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

/**
 * 失败回调方法处理类
 *
 * @author liuyang
 * 创建时间: 2022-07-25 10:13
 */
public class FallbackMethodHandler implements MethodHandler {

    private final MethodMetadata metadata;

    private final MethodHandler delegate;

    private final FallbackFactory fallbackFactory;

    public FallbackMethodHandler(MethodMetadata metadata, MethodHandler delegate, FallbackFactory fallbackFactory) {
        this.metadata = metadata;
        this.delegate = delegate;
        this.fallbackFactory = fallbackFactory;
    }

    @Override
    public Object invoke(Object[] argv) {
        Publisher<?> publisher;
        try {
            publisher = (Publisher<?>) delegate.invoke(argv);
        } catch (Throwable throwable) {
            publisher = Mono.error(throwable);
        }

        Method method = metadata.getMethod();
        if (metadata.isFlux()) {
            return ((Flux<Object>) publisher).onErrorResume(throwable -> {
                Object fallback = fallbackFactory.apply(throwable);
                Object fallbackValue = getFallbackValue(fallback, method, argv);
                return (Publisher<Object>) fallbackValue;
            });
        } else {
            return ((Mono<Object>) publisher).onErrorResume(throwable -> {
                Object fallback = fallbackFactory.apply(throwable);
                Object fallbackValue = getFallbackValue(fallback, method, argv);
                return (Mono<Object>) fallbackValue;
            });
        }
    }

    private Object getFallbackValue(Object target, Method method, Object[] argv) {
        try {
            return method.invoke(target, argv);
        } catch (Throwable e) {
            throw Exceptions.propagate(e);
        }
    }
}
