package fegin.parameter;

import fegin.domain.MethodMetadata;
import fegin.parameter.IParameterProcessor.Default;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数处理工具类，根据参数上的注解执行不同的处理器，如果不存在则按RequestParam处理
 *
 * @author liuyang
 * 创建时间: 2022-07-23 15:37
 */
public class ParameterProcessorUtils {
    private static Map<Class<? extends Annotation>, IParameterProcessor> result = new HashMap<>();

    private ParameterProcessorUtils() {
    }

    static {
        List<IParameterProcessor> processors = Arrays.asList(
                new RequestParamParameterProcessor(),
                new PathVariableParameterProcessor(),
                new RequestBodyParameterProcessor(),
                new RequestHeaderParameterProcessor(),
                new DefaultParameterProcessor());
        processors.forEach(processor -> result.put(processor.getAnnotation(), processor));
    }

    public static void registerProcessor(IParameterProcessor processor) {
        result.put(processor.getAnnotation(), processor);
    }

    public static void processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        if (annotations == null || annotations.length == 0) {
            processDefault(data, paramIndex);
            return;
        }
        for (Annotation parameterAnnotation : annotations) {
            processAnnotation(data, parameterAnnotation, paramIndex);
        }
    }

    public static void processAnnotation(MethodMetadata data, Annotation parameterAnnotation, int paramIndex) {
        Class<? extends Annotation> type = parameterAnnotation.annotationType();
        IParameterProcessor parameterProcessor = result.get(type);
        if (parameterProcessor != null) {
            parameterProcessor.processArgument(data, parameterAnnotation, paramIndex);
        }
    }

    public static void processDefault(MethodMetadata data, int paramIndex) {
        IParameterProcessor defaultProcessor = result.get(Default.class);
        if (defaultProcessor != null) {
            defaultProcessor.processArgument(data, null, paramIndex);
        }
    }
}
