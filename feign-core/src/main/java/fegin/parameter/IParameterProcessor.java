package fegin.parameter;

import fegin.domain.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数处理器统一接口
 *
 * @author liuyang
 * 创建时间: 2022-07-23 15:40
 */
public interface IParameterProcessor {

    /**
     * Processor对应的注解
     *
     * @return 对应注解
     */
    Class<? extends Annotation> getAnnotation();

    /**
     * 处理注解
     *
     * @param data                MethodMetadata
     * @param parameterAnnotation 注解
     * @param paramIndex          序号
     */
    void processArgument(MethodMetadata data, Annotation parameterAnnotation, int paramIndex);

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Default {

    }
}
