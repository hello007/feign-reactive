package fegin.domain;

import java.util.List;
import java.util.function.Supplier;

/**
 * WebClient可选配置定义类
 * <p>
 * 返回ClientOption列表集合
 *
 * @author liuyang
 * 创建时间: 2022-07-26 11:28
 */
public interface ClientOptionDefinition extends Supplier<List<ClientOption<?>>> {
}
