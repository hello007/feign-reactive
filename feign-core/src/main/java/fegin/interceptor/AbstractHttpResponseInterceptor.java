package fegin.interceptor;

/**
 * 默认ReactiveHttpResponseInterceptor抽象实现类，通过构造函数传入order级别进行优先级排序
 *
 * @author liuyang
 * 创建时间: 2022-07-24 16:56
 */
public abstract class AbstractHttpResponseInterceptor implements ReactiveHttpResponseInterceptor {

    private final int order;

    public AbstractHttpResponseInterceptor(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
