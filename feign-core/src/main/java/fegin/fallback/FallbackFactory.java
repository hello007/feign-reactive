package fegin.fallback;

import java.util.function.Function;

/**
 * fallback工厂类
 *
 * @author liuyang
 * 创建时间: 2022-07-25 10:51
 */
public interface FallbackFactory extends Function<Throwable, Object> {
}
