package fegin.parameter;

import fegin.domain.MethodMetadata;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * RequestParam处理类
 *
 * @author liuyang
 * 创建时间: 2022-07-23 15:48
 */
public class RequestParamParameterProcessor implements IParameterProcessor {

    private static final Class<RequestParam> ANNOTATION = RequestParam.class;

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return ANNOTATION;
    }

    @Override
    public void processArgument(MethodMetadata data, Annotation parameterAnnotation, int paramIndex) {
        Method method = data.getMethod();
        RequestParam requestParam = ANNOTATION.cast(parameterAnnotation);
        String value = requestParam.value();
        if (StringUtils.isEmpty(value)) {
            Parameter parameter = method.getParameters()[paramIndex];
            value = parameter.getName();
        }
        data.addParamIndex(paramIndex, value);
    }
}
