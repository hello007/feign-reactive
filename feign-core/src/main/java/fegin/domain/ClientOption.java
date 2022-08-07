package fegin.domain;

import io.netty.channel.ChannelOption;

/**
 * 客户端可选配置类
 * <p>
 * 1. 连接超时：{@link ChannelOption#CONNECT_TIMEOUT_MILLIS}
 *
 * @author liuyang
 * 创建时间: 2022-07-26 11:22
 */
public class ClientOption<T> {

    public static final ChannelOption<Integer> READ_TIMEOUT_OPTION = ChannelOption.valueOf("FeignReadTimeout");

    public static final ChannelOption<Integer> WRITE_TIMEOUT_OPTION = ChannelOption.valueOf("FeignWriteTimeout");

    private ChannelOption<T> channelOption;

    private T t;

    public ClientOption(ChannelOption<T> channelOption, T t) {
        this.channelOption = channelOption;
        this.t = t;
    }

    public ChannelOption<T> getChannelOption() {
        return channelOption;
    }

    public T getT() {
        return t;
    }
}
