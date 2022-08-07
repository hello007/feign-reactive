package fegin.utils;

import fegin.domain.MethodMetadata;
import fegin.parameter.ParameterProcessorUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * 注解工具类，主要作用是将FeignClient标记的接口的方法解析为{@link MethodMetadata}
 *
 * @author liuyang
 * 创建时间: 2022-07-22 15:13
 */
public class AnnotationUtils {

    private static final List<Class<?>> REACTOR_PUBLISHER = Arrays.asList(Mono.class, Flux.class);

    private AnnotationUtils() {
    }

    /**
     * 将接口内所有方法、方法注解、参数、参数注解等解析为MethodMetadata
     *
     * @param targetType 接口类
     * @param prefixUrl  公共前缀url（url+path）
     * @return 所有方法的MethodMetadata集合
     */
    public static List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType, String prefixUrl) {
        Map<String, MethodMetadata> result = new LinkedHashMap<>();
        for (final Method method : targetType.getMethods()) {
            if (method.getDeclaringClass() == Object.class ||
                    (method.getModifiers() & Modifier.STATIC) != 0 || isDefault(method)) {
                continue;
            }
            MethodMetadata metadata = parseAndValidateMetadata(targetType, method);
            metadata.setPrefixUrl(prefixUrl);
            String configKey = metadata.getConfigKey();
            if (result.containsKey(configKey)) {
                MethodMetadata existingMetadata = result.get(configKey);
                Type existingReturnType = existingMetadata.getReturnType();
                Type overridingReturnType = metadata.getReturnType();
                Type resolvedType = resolveReturnType(existingReturnType, overridingReturnType);
                if (resolvedType.equals(overridingReturnType)) {
                    result.put(configKey, metadata);
                }
            } else {
                result.put(configKey, metadata);
            }
        }
        return new ArrayList<>(result.values());
    }

    /**
     * 解析方法、返回值、方法注解
     *
     * @param targetType 接口类
     * @param method     方法
     * @return 方法对应的MethodMetadata
     */
    public static MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        MethodMetadata data = new MethodMetadata();
        String name = method.getName();
        data.setTargetType(targetType);
        data.setMethod(method);

        ParameterizedType parameterizedType = validAndGetReturnType(targetType, method);
        Type[] arguments = parameterizedType.getActualTypeArguments();
        if (arguments != null && arguments.length == 1) {
            Class<?> rawType = getRawType(arguments[0]);
            if (rawType == ResponseEntity.class) {
                ParameterizedType innerType = (ParameterizedType) arguments[0];
                Type[] actualTypeArguments = innerType.getActualTypeArguments();
                data.setResponseEntity(true);
                if (actualTypeArguments == null || actualTypeArguments.length == 0) {
                    data.setReturnType(Object.class);
                } else if (actualTypeArguments.length == 1) {
                    data.setReturnType(actualTypeArguments[0]);
                } else {
                    String msg = String.format("%s#%s返回类型为%s，请检查", targetType, name, Arrays.toString(arguments));
                    throw new IllegalArgumentException(msg);
                }
            } else {
                data.setReturnType(arguments[0]);
            }
        } else {
            String msg = String.format("%s#%s返回类型为%s，请检查", targetType, name, Arrays.toString(arguments));
            throw new IllegalArgumentException(msg);
        }
        Type rawType = parameterizedType.getRawType();
        if (Flux.class == rawType) {
            data.tagFlux();
        } else {
            data.tagMono();
        }
        data.setConfigKey(configKey(targetType, method));

        for (final Annotation methodAnnotation : method.getAnnotations()) {
            processAnnotationOnMethod(data, methodAnnotation, method);
        }
        return data;
    }

    /**
     * 解析方法上标记的RequestMapping相关注解，并解析方法内相关参数对象
     *
     * @param data             方法的MethodMetadata对象
     * @param methodAnnotation 注解，只解析RequestMapping
     * @param method           方法
     */
    private static void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
        if (!(methodAnnotation instanceof RequestMapping)
                && !methodAnnotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
            return;
        }
        RequestMapping methodMapping = findMergedAnnotation(method, RequestMapping.class);
        if (methodMapping == null) {
            return;
        }
        RequestMethod[] methods = methodMapping.method();
        if (methods.length == 0) {
            methods = new RequestMethod[]{RequestMethod.GET};
        }
        if (methods.length != 1) {
            throw new IllegalArgumentException(String.format("%s包含多个请求方式%s，请检查", data.getConfigKey(), Arrays.toString(methods)));
        }
        data.setHttpMethod(methods[0].name());
        String[] value = methodMapping.value();
        if (value.length < 1) {
            throw new IllegalArgumentException(String.format("%s未包含请求value，请检查", data.getConfigKey()));
        }
        // 解析path路径
        String pathValue = value[0];
        if (!StringUtils.isEmpty(pathValue)) {
            if (!pathValue.startsWith("/")) {
                pathValue = "/" + pathValue;
            }
            data.setPath(pathValue);
        }
        // 解析header
        String[] headers = methodMapping.headers();
        if (headers.length > 0) {
            for (String header : headers) {
                int index = header.indexOf('=');
                if (!header.contains("!=") && index >= 0) {
                    data.addHeader(header.substring(0, index),
                            header.substring(index + 1).trim());
                }
            }
        }
        // 解析produces
        String[] produces = methodMapping.produces();
        if (produces.length > 0) {
            data.addHeader(HttpHeaders.ACCEPT, produces[0]);
        }
        // 解析consumes
        String[] consumes = methodMapping.consumes();
        if (consumes.length > 0) {
            data.addHeader(HttpHeaders.CONTENT_TYPE, consumes[0]);
        }

        // 解析方法参数
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int length = parameterAnnotations.length;
        for (int i = 0; i < length; i++) {
            ParameterProcessorUtils.processAnnotationsOnParameter(data, parameterAnnotations[i], i);
        }
    }

    /**
     * 校验方法返回值是否为Publisher对象
     *
     * @param targetType 接口类
     * @param method     校验的方法
     * @return 方法返回值的Type类型
     */
    private static ParameterizedType validAndGetReturnType(Class<?> targetType, Method method) {
        String name = method.getName();
        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            String msg = String.format("%s#%s返回类型非ParameterizedType，请检查", targetType, name);
            throw new IllegalArgumentException(msg);
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
        if (!isReactorType(parameterizedType)) {
            String msg = String.format("%s#%s返回类型非Mono or Flux，请检查", targetType, name);
            throw new IllegalArgumentException(msg);
        }
        return parameterizedType;
    }

    /**
     * 获得方法唯一标志
     *
     * @param targetType 接口类
     * @param method     方法
     * @return 唯一标志
     */
    public static String configKey(Class<?> targetType, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetType.getSimpleName());
        builder.append('#').append(method.getName()).append('(');
        for (Type param : method.getGenericParameterTypes()) {
            builder.append(getRawType(param).getSimpleName()).append(',');
        }
        if (method.getParameterTypes().length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.append(')').toString();
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException();
            }
            return (Class<?>) rawType;
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            return Object.class;
        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type "
                    + className);
        }
    }

    /**
     * 判断传入的Type是否为Publisher对象
     *
     * @param type 要判断的type
     * @return true: Mono、Flux对象；false：非Publisher对象
     */
    public static boolean isReactorType(final ParameterizedType type) {
        Type rawType = type.getRawType();
        return REACTOR_PUBLISHER.contains(rawType);
    }

    /**
     * 是否是Default标记的接口方法
     */
    public static boolean isDefault(Method method) {
        // Default methods are public non-abstract, non-synthetic, and non-static instance methods
        // declared in an interface.
        // method.isDefault() is not sufficient for our usage as it does not check
        // for synthetic methods. As a result, it picks up overridden methods as well as actual default
        // methods.
        final int SYNTHETIC = 0x00001000;
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    public static Type resolveReturnType(Type baseType, Type overridingType) {
        if (baseType instanceof Class && overridingType instanceof Class &&
                ((Class<?>) baseType).isAssignableFrom((Class<?>) overridingType)) {
            // NOTE: javac generates multiple same methods for multiple inherited generic interfaces
            return overridingType;
        }
        if (baseType instanceof Class && overridingType instanceof ParameterizedType) {
            // NOTE: javac will generate multiple methods with different return types
            // base interface declares generic method, override declares parameterized generic method
            return overridingType;
        }
        if (baseType instanceof Class && overridingType instanceof TypeVariable) {
            // NOTE: javac will generate multiple methods with different return types
            // base interface declares non generic method, override declares generic method
            return overridingType;
        }
        return baseType;
    }
}
