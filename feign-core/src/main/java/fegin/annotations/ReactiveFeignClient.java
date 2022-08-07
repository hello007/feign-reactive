package fegin.annotations;

import fegin.config.ReactiveFeignClientsConfiguration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for interfaces declaring that a REST client with that interface should be
 * created (e.g. for autowiring into another component).
 * <p>
 * patterned after org.springframework.cloud.netflix.feign.FeignClient
 *
 * @author playtika
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReactiveFeignClient {

    /**
     * The name of the service with optional protocol prefix. Synonym for {@link #name()
     * name}. A name must be specified for all clients, whether or not a url is provided.
     * Can be specified as property key, eg: ${propertyKey}.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The service id with optional protocol prefix. Synonym for {@link #value() value}.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * Sets the <code>@Qualifier</code> value for the feign client.
     */
    String qualifier() default "";

    /**
     * 访问的url，暂时只支持绝对url或域名（不支持注册中心方式）
     * An absolute URL or resolvable hostname (the protocol is optional).
     */
    String url() default "";

    /**
     * feign client自定义的配置类，如注入自定义Interceptor、fallback等<br>
     * A custom <code>@Configuration</code> for the feign client. Can contain override
     * <code>@Bean</code> definition for the pieces that make up the client, for instance
     * {@link fegin.interceptor.ReactiveHttpHeaderInterceptor}.
     *
     * @see ReactiveFeignClientsConfiguration for the defaults
     */
    Class<?>[] configuration() default {};

    /**
     * Fallback class for the specified Feign client interface. The fallback class must
     * implement the interface annotated by this annotation and be a valid spring bean.
     */
    Class<?> fallback() default void.class;

    /**
     * fallback工厂类，需实现{@link fegin.fallback.FallbackFactory}
     * Define a fallback factory for the specified Feign client interface. The fallback
     * factory must produce instances of fallback classes that implement the interface
     * annotated by {@link ReactiveFeignClient}. The fallback factory must be a valid spring
     * bean.
     */
    Class<?> fallbackFactory() default void.class;

    /**
     * 类内所有方法的统一前缀路径<br>
     * Path prefix to be used by all method-level mappings. Can be used with or without
     * <code>@RibbonClient</code>.
     */
    String path() default "";

    /**
     * Whether to mark the feign proxy as a primary bean. Defaults to true.
     */
    boolean primary() default true;

}
