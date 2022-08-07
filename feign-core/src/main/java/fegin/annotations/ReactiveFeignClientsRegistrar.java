package fegin.annotations;

import fegin.config.ReactiveFeignClientSpecification;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义ReactiveFeign注册器
 */
class ReactiveFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    public static final String DEFAULT_CONFIGURATION = "defaultConfiguration";

    public static final String LEFT_SLASH = "/";

    public static final String FALLBACK = "fallback";

    public static final String FALLBACK_FACTORY = "fallbackFactory";

    public static final String VALUE = "value";

    private ResourceLoader resourceLoader;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registerDefaultConfiguration(metadata, registry);
        registerReactiveFeignClients(metadata, registry);
    }

    /**
     * 注册默认的配置类到spring容器内
     *
     * @param metadata EnableReactiveFeignClients对应的注解元数据
     * @param registry bean注册器
     */
    private void registerDefaultConfiguration(AnnotationMetadata metadata,
                                              BeanDefinitionRegistry registry) {
        Map<String, Object> defaultAttrs = metadata
                .getAnnotationAttributes(EnableReactiveFeignClients.class.getName(), true);

        if (defaultAttrs != null && defaultAttrs.containsKey(DEFAULT_CONFIGURATION)) {
            String name;
            if (metadata.hasEnclosingClass()) {
                name = "default." + metadata.getEnclosingClassName();
            } else {
                name = "default." + metadata.getClassName();
            }
            registerClientConfiguration(registry, name,
                    defaultAttrs.get(DEFAULT_CONFIGURATION));
        }
    }

    /**
     * 注册对应的configuration类到spring中，类型为ReactiveFeignClientSpecification
     *
     * @param registry      bean注册器
     * @param name          注册名
     * @param configuration 要注册的configuration类
     */
    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name,
                                             Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(ReactiveFeignClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(
                name + "." + ReactiveFeignClientSpecification.class.getSimpleName(),
                builder.getBeanDefinition());
    }

    /**
     * 扫描所有标记ReactiveFeignClient的接口类并注册到spring中
     *
     * @param metadata 注解元数据
     * @param registry bean注册器
     */
    private void registerReactiveFeignClients(AnnotationMetadata metadata,
                                              BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        Set<String> basePackages;

        Map<String, Object> attrs = metadata
                .getAnnotationAttributes(EnableReactiveFeignClients.class.getName());
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                ReactiveFeignClient.class);
        final Class<?>[] clients = attrs == null ? null
                : (Class<?>[]) attrs.get("clients");
        if (clients == null || clients.length == 0) {
            scanner.addIncludeFilter(annotationTypeFilter);
            basePackages = getBasePackages(metadata);
        } else {
            final Set<String> clientClasses = new HashSet<>();
            basePackages = new HashSet<>();
            for (Class<?> clazz : clients) {
                basePackages.add(ClassUtils.getPackageName(clazz));
                clientClasses.add(clazz.getCanonicalName());
            }
            AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
                @Override
                protected boolean match(ClassMetadata metadata) {
                    String cleaned = metadata.getClassName().replace("\\$", ".");
                    return clientClasses.contains(cleaned);
                }
            };
            scanner.addIncludeFilter(
                    new AllTypeFilter(Arrays.asList(filter, annotationTypeFilter)));
        }

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(),
                            "@ReactiveFeignClient can only be specified on an interface");

                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(
                                    ReactiveFeignClient.class.getCanonicalName());
                    if (attributes == null) {
                        continue;
                    }
                    String name = getClientName(attributes);
                    registerClientConfiguration(registry, name,
                            attributes.get("configuration"));

                    registerReactiveFeignClient(registry, annotationMetadata, attributes);
                }
            }
        }

    }

    /**
     * 注册被ReactiveFeignClient标记的接口到spring中
     *
     * @param registry           bean注册器
     * @param annotationMetadata 类上的注解元数据
     * @param attributes         ReactiveFeignClient的属性
     */
    private void registerReactiveFeignClient(BeanDefinitionRegistry registry,
                                             AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(ReactiveFeignClientFactoryBean.class);
        validate(attributes);
        definition.addPropertyValue("url", getUrl(attributes));
        definition.addPropertyValue("path", getPath(attributes));
        String name = getName(attributes);
        definition.addPropertyValue("name", name);
        definition.addPropertyValue("type", className);
        definition.addPropertyValue(FALLBACK, attributes.get(FALLBACK));
        definition.addPropertyValue(FALLBACK_FACTORY, attributes.get(FALLBACK_FACTORY));
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        String alias = name + "ReactiveFeignClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, className);

        // has a default, won't be null
        boolean primary = (Boolean) attributes.get("primary");

        beanDefinition.setPrimary(primary);

        String qualifier = getQualifier(attributes);
        if (StringUtils.hasText(qualifier)) {
            alias = qualifier;
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private String getUrl(Map<String, Object> attributes) {
        // 这里直接返回，后面如果扩展在这里修改即可
        return resolve((String) attributes.get("url"));
    }

    private String getPath(Map<String, Object> attributes) {
        String path = resolve((String) attributes.get("path"));
        return getPath(path);
    }

    private String getName(Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            name = (String) attributes.get(VALUE);
        }
        name = resolve(name);
        // 这里直接返回name
        return name;
    }

    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String qualifier = (String) client.get("qualifier");
        if (StringUtils.hasText(qualifier)) {
            return qualifier;
        }
        return null;
    }

    static String getPath(String path) {
        if (StringUtils.hasText(path)) {
            path = path.trim();
            if (!path.startsWith(LEFT_SLASH)) {
                path = LEFT_SLASH + path;
            }
            path = cutTail(path, LEFT_SLASH);
        }
        return path;
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
            }
        };
    }

    /**
     * 获取EnableReactiveFeignClients注解标注的所有package路径
     *
     * @param importingClassMetadata 注解对应的AnnotationMetadata
     * @return package的集合列表
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableReactiveFeignClients.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if (attributes == null) {
            return basePackages;
        }
        for (String pkg : (String[]) attributes.get(VALUE)) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class<?>[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private String getClientName(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String value = (String) client.get(VALUE);
        if (!StringUtils.hasText(value)) {
            value = (String) client.get("name");
        }
        if (StringUtils.hasText(value)) {
            return value;
        }

        throw new IllegalStateException("Either 'name' or 'value' must be provided in @"
                + ReactiveFeignClient.class.getSimpleName());
    }

    public static String cutTail(String str, String tail) {
        return str.endsWith(tail)
                ? str.substring(0, str.length() - tail.length())
                : str;
    }

    private void validate(Map<String, Object> attributes) {
        AnnotationAttributes annotation = AnnotationAttributes.fromMap(attributes);
        if (annotation == null) {
            return;
        }
        validateFallback(annotation.getClass(FALLBACK));
        validateFallbackFactory(annotation.getClass(FALLBACK_FACTORY));
    }

    static void validateFallback(final Class<?> clazz) {
        Assert.isTrue(
                !clazz.isInterface(),
                "Fallback class must implement the interface annotated by @FeignClient"
        );
    }

    static void validateFallbackFactory(final Class<?> clazz) {
        Assert.isTrue(!clazz.isInterface(),
                "Fallback factory must produce instances of fallback classes that implement the interface annotated by @FeignClient"
        );
    }

    private static class AllTypeFilter implements TypeFilter {

        private final List<TypeFilter> delegates;

        /**
         * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
         *
         * @param delegates must not be {@literal null}.
         */
        public AllTypeFilter(List<TypeFilter> delegates) {
            Assert.notNull(delegates, "This argument is required, it must not be null");
            this.delegates = delegates;
        }

        @Override
        public boolean match(MetadataReader metadataReader,
                             MetadataReaderFactory metadataReaderFactory) throws IOException {

            for (TypeFilter filter : this.delegates) {
                if (!filter.match(metadataReader, metadataReaderFactory)) {
                    return false;
                }
            }

            return true;
        }
    }
}
