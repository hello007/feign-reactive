package fegin.annotations;

import fegin.config.ReactiveFeignConfigurator;
import fegin.config.ReactiveFeignNamedContext;
import fegin.domain.MethodMetadata;
import fegin.domain.ReactiveFeign;
import fegin.domain.Target;
import fegin.fallback.FallbackFactory;
import fegin.utils.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 创建被{@link ReactiveFeignClient}标记的接口代理类并注入到Spring中
 */
class ReactiveFeignClientFactoryBean implements FactoryBean<Object>, InitializingBean,
        ApplicationContextAware {

    /**
     * 接口类名
     */
    private Class<?> type;

    /**
     * ReactiveFeignClient名，隔离NamedContextFactory使用
     */
    private String name;

    /**
     * 定义的url
     */
    private String url;

    /**
     * 公共urlPath前缀
     */
    private String path = "";

    private ApplicationContext applicationContext;

    private Class<?> fallback = void.class;

    private Class<?> fallbackFactory = void.class;

    @Override
    public Object getObject() {
        ReactiveFeignNamedContext namedContext = new ReactiveFeignNamedContext(applicationContext, name);
        String prefixUrl = url + path;
        List<MethodMetadata> methodMetadata = AnnotationUtils.parseAndValidateMetadata(type, prefixUrl);
        Target target = new Target(type, prefixUrl);
        ReactiveFeign reactiveFeign = new ReactiveFeign(target, methodMetadata);

        applyConfigurations(reactiveFeign, namedContext);
        applyFallback(reactiveFeign, namedContext);

        return reactiveFeign.newInstance();
    }

    /**
     * 获得当前FeignClient使用的自定义配置并配置feign对象
     *
     * @param feign        要配置/修改的feign对象
     * @param namedContext 使用的NamedContext对象
     */
    private void applyConfigurations(ReactiveFeign feign, ReactiveFeignNamedContext namedContext) {
        Map<String, ReactiveFeignConfigurator> allConfigurations = namedContext.getAll(ReactiveFeignConfigurator.class);
        List<ReactiveFeignConfigurator> configurators = allConfigurations.values().stream().sorted().collect(Collectors.toList());
        for (ReactiveFeignConfigurator configurator : configurators) {
            configurator.configure(feign, namedContext);
        }
    }

    /**
     * 获取自定义的fallback对象，支持自定义或工厂类获取
     *
     * @param reactiveFeign feign对象
     * @param namedContext  使用的NamedContext对象
     */
    private void applyFallback(ReactiveFeign reactiveFeign, ReactiveFeignNamedContext namedContext) {
        FallbackFactory fallbackFactoryInstance = null;
        if (fallback != void.class) {
            Object fallbackInstance = getFallbackFromContext(
                    "fallback", namedContext, this.fallback, this.type);
            fallbackFactoryInstance = throwable -> fallbackInstance;
        } else if (fallbackFactory != void.class) {
            fallbackFactoryInstance = (FallbackFactory) getFallbackFromContext(
                    "fallbackFactory", namedContext, fallbackFactory, FallbackFactory.class);
        }
        if (fallbackFactoryInstance != null) {
            reactiveFeign.setFallbackFactory(fallbackFactoryInstance);
        }
    }

    /**
     * 从NamedContextFactory获得定义的Fallback/fallbackFactory对象
     *
     * @param fallbackMechanism 报错描述字符串
     * @param context           name对应的ReactiveFeignNamedContext对象
     * @param beanType          获取的对象类型
     * @param targetType        目标对象类型，用于校验
     * @return spring中beanType对应的bean对象
     */
    private Object getFallbackFromContext(String fallbackMechanism, ReactiveFeignNamedContext context,
                                          Class<?> beanType, Class<?> targetType) {
        Object fallbackInstance = context.getOptional(beanType);
        if (!targetType.isAssignableFrom(beanType)) {
            throw new IllegalStateException(
                    String.format(
                            "Incompatible %s instance. Fallback/fallbackFactory of type %s is not assignable to %s for feign client %s",
                            fallbackMechanism, beanType, targetType, context.getClientName()));
        }
        if (fallbackInstance == null) {
            fallbackInstance = context.getOrInstantiate(beanType);
        }
        return fallbackInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.hasText(this.name, "name不允许为空");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url != null && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    public Class<?> getFallback() {
        return fallback;
    }

    public void setFallback(Class<?> fallback) {
        this.fallback = fallback;
    }

    public Class<?> getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(Class<?> fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }
}
