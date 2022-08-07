package fegin.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * 包装ReactiveFeignNamedContextFactory，用于不同name获取不同的配置类
 *
 * @author liuyang
 * 创建时间: 2022-07-22 13:58
 */
public class ReactiveFeignNamedContext {
    private final ApplicationContext applicationContext;
    private final ReactiveFeignNamedContextFactory namedContextFactory;
    private final String clientName;

    public ReactiveFeignNamedContext(ApplicationContext applicationContext, String clientName) {
        this.applicationContext = applicationContext;
        this.clientName = clientName;
        this.namedContextFactory = applicationContext.getBean(ReactiveFeignNamedContextFactory.class);
    }

    public <T> T get(Class<T> type) {
        T instance = namedContextFactory.getInstance(this.clientName, type);
        if (instance == null) {
            throw new IllegalStateException("No bean found of type " + type + " for "
                    + this.clientName);
        }
        return instance;
    }

    public <T> Map<String, T> getAll(Class<T> type) {
        Map<String, T> instances = namedContextFactory.getInstances(this.clientName, type);
        return instances != null ? instances : emptyMap();
    }

    public <T> T getOptional(Class<T> type) {
        return namedContextFactory.getInstance(this.clientName, type);
    }

    public <T> T getOptional(Class<T> type, String beanName) {
        Map<String, T> instances = namedContextFactory.getInstances(this.clientName, type);
        if (instances == null) {
            return null;
        }
        return instances.get(beanName);
    }

    public <T> T getOrInstantiate(Class<T> tClass) {
        try {
            return applicationContext.getBean(tClass);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(tClass);
        }
    }

    public String getClientName() {
        return clientName;
    }
}
