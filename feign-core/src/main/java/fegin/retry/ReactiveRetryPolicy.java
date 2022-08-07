package fegin.retry;

import fegin.domain.MethodMetadata;
import reactor.util.retry.Retry;

/**
 * 重试策略，支持按方法来定义不同的Retry策略
 *
 * @author liuyang
 * 创建时间: 2022-07-25 10:33
 */
public interface ReactiveRetryPolicy {

    /**
     * 自定义retry策略
     *
     * @param metadata 方法对应的元数据
     * @return retry策略
     */
    Retry retry(MethodMetadata metadata);
}
