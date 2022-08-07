package fegin.config;

/**
 * ReactiveFeignConfigurator接口实现类，支持Order定义及排序
 *
 * @author liuyang
 * 创建时间: 2022-07-24 13:36
 */
public abstract class AbstractReactiveFeignConfigurator implements ReactiveFeignConfigurator {

    private final int order;

    public AbstractReactiveFeignConfigurator(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(ReactiveFeignConfigurator configurator) {
        int compare = Integer.compare(order, ((AbstractReactiveFeignConfigurator) configurator).order);
        if (compare == 0) {
            throw new IllegalArgumentException(String.format("Same order for different configurators: [%s], [%s]",
                    this, configurator));
        }
        return compare;
    }
}
