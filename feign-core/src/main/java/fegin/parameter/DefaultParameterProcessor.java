package fegin.parameter;

import fegin.domain.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 方法参数无注解修饰时的默认处理器，按RequestParam来处理
 *
 * @author liuyang
 * 创建时间: 2022-07-24 10:52
 */
public class DefaultParameterProcessor implements IParameterProcessor {
    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Default.class;
    }

    @Override
    public void processArgument(MethodMetadata data, Annotation parameterAnnotation, int paramIndex) {
        Method method = data.getMethod();
        Parameter parameter = method.getParameters()[paramIndex];
        String value = parameter.getName();
        data.addParamIndex(paramIndex, value);
    }
}
