package fegin.parameter;

import fegin.domain.MethodMetadata;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;

/**
 * RequestBody处理类
 *
 * @author liuyang
 * 创建时间: 2022-07-26 20:17
 */
public class RequestBodyParameterProcessor implements IParameterProcessor {

    private static final Class<? extends RequestBody> ANNOTATION = RequestBody.class;

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return ANNOTATION;
    }

    @Override
    public void processArgument(MethodMetadata data, Annotation parameterAnnotation, int paramIndex) {
        data.setBodyIndex(paramIndex);
    }
}
