package fegin.interceptor;

/**
 * 默认ReactiveHttpRequestInterceptor抽象实现类，通过构造函数传入order级别
 *
 * @author liuyang
 * 创建时间: 2022-07-24 16:56
 */
public abstract class AbstractHttpRequestInterceptor implements ReactiveHttpRequestInterceptor {

    private final int order;

    public AbstractHttpRequestInterceptor(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
