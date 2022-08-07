package fegin.parameter;

import fegin.domain.MethodMetadata;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * PathVariable处理类
 *
 * @author liuyang
 * 创建时间: 2022-07-24 11:15
 */
public class PathVariableParameterProcessor implements IParameterProcessor {

    private static final Class<PathVariable> ANNOTATION = PathVariable.class;

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return ANNOTATION;
    }

    @Override
    public void processArgument(MethodMetadata data, Annotation parameterAnnotation, int paramIndex) {
        Method method = data.getMethod();
        String value = ANNOTATION.cast(parameterAnnotation).value();
        if (StringUtils.isEmpty(value)) {
            Parameter parameter = method.getParameters()[paramIndex];
            value = parameter.getName();
        }
        data.addVariableIndex(paramIndex, value);
    }
}
