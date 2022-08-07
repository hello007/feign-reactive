package fegin.handler;

import fegin.domain.MethodMetadata;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 方法异常重试处理类，根据定义的{@link fegin.retry.ReactiveRetryPolicy}来实现重试策略
 *
 * @author liuyang
 * 创建时间: 2022-07-25 10:24
 */
public class RetryMethodHandler implements MethodHandler {

    private final MethodMetadata metadata;

    private final MethodHandler delegate;

    private final Retry retry;

    public RetryMethodHandler(MethodMetadata metadata, MethodHandler delegate, Retry retry) {
        this.metadata = metadata;
        this.delegate = delegate;
        this.retry = retry;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        Publisher<?> invokeResult = (Publisher<?>) delegate.invoke(argv);
        if (metadata.isFlux()) {
            return Flux.from(invokeResult).retryWhen(retry);
        }
        return Mono.from(invokeResult).retryWhen(retry);
    }
}
