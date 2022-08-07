package fegin.annotations;

import fegin.config.ReactiveFeignAutoConfiguration;
import fegin.config.ReactiveFeignClientsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启Feign并自动扫描所有的{@link ReactiveFeignClient}注解的bean并自动代理
 * <p>
 * Scans for interfaces that declare they are reactive feign clients (via {@link ReactiveFeignClient
 * <code>@ReactiveFeignClient</code>}). Configures component scanning directives for use with
 * {@link org.springframework.context.annotation.Configuration
 * <code>@Configuration</code>} classes.
 * <p>
 * patterned after org.springframework.cloud.netflix.feign.EnableFeignClients
 *
 * @author playtika
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ReactiveFeignClientsRegistrar.class, ReactiveFeignAutoConfiguration.class})
public @interface EnableReactiveFeignClients {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation
     * declarations e.g.: {@code @ComponentScan("org.my.pkg")} instead of
     * {@code @ComponentScan(basePackages="org.my.pkg")}.
     *
     * @return the array of 'basePackages'.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components.
     * <p>
     * {@link #value()} is an alias for (and mutually exclusive with) this attribute.
     * <p>
     * Use {@link #basePackageClasses()} for a type-safe alternative to String-based
     * package names.
     *
     * @return the array of 'basePackages'.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to
     * scan for annotated components. The package of each class specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that
     * serves no purpose other than being referenced by this attribute.
     *
     * @return the array of 'basePackageClasses'.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * A custom <code>@Configuration</code> for all feign clients. Can contain override
     * <code>@Bean</code> definition for the pieces that make up the client,
     * {@link fegin.interceptor.ReactiveHttpRequestInterceptor}.
     *
     * @see ReactiveFeignClientsConfiguration for the defaults
     */
    Class<?>[] defaultConfiguration() default {};

    /**
     * List of classes annotated with @ReactiveFeignClient. If not empty, disables classpath scanning.
     *
     * @return
     */
    Class<?>[] clients() default {};
}
