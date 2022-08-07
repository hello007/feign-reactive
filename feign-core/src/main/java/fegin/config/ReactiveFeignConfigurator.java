package fegin.config;

import fegin.domain.ReactiveFeign;

/**
 * 配置ReactiveFeign相关属性
 *
 * @author liuyang
 * 创建时间: 2022-07-24 13:35
 */
public interface ReactiveFeignConfigurator extends Comparable<ReactiveFeignConfigurator> {

    /**
     * 配置ReactiveFeign对象
     *
     * @param feign
     * @param namedContext
     */
    void configure(ReactiveFeign feign, ReactiveFeignNamedContext namedContext);
}
