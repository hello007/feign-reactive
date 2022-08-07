package fegin.domain;

import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 方法元数据对象
 *
 * @author liuyang
 * 创建时间: 2022-07-22 15:14
 */
public class MethodMetadata {

    enum PublishType {
        mono, flux;
    }

    private String configKey;

    private Class<?> targetType;

    private String prefixUrl;

    private Method method;

    /**
     * 返回类型
     */
    private Type returnType;

    /**
     * 返回类型是否是ResponseEntity对象
     */
    private boolean responseEntity;

    private HttpMethod httpMethod;

    private String path;

    /**
     * 默认请求头
     */
    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

    /**
     * @RequestHeader 注解标记的方法参数所在位置
     */
    private Map<Integer, String> headerIndexNameMap = new LinkedHashMap<>();

    /**
     * @RequestParam 注解标记的方法参数所在位置
     */
    private Map<Integer, String> paramIndexNameMap = new LinkedHashMap<>();

    /**
     * @PathVariable 注解标记的方法参数所在位置
     */
    private Map<Integer, String> variableIndexNameMap = new LinkedHashMap<>();

    /**
     * @RequestBody 注解标记的方法参数所在位置
     */
    private int bodyIndex = -1;

    private PublishType publishType = PublishType.mono;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getPrefixUrl() {
        return prefixUrl;
    }

    public void setPrefixUrl(String prefixUrl) {
        this.prefixUrl = prefixUrl;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void tagMono() {
        this.publishType = PublishType.mono;
    }

    public void tagFlux() {
        this.publishType = PublishType.flux;
    }

    public boolean isMono() {
        return this.publishType == PublishType.mono;
    }

    public boolean isFlux() {
        return this.publishType == PublishType.flux;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = HttpMethod.valueOf(httpMethod.toUpperCase());
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        this.headers.add(key, value);
    }

    public Map<Integer, String> getParamIndexNameMap() {
        return paramIndexNameMap;
    }

    public void addParamIndex(int index, String name) {
        this.paramIndexNameMap.put(index, name);
    }

    public Map<Integer, String> getHeaderIndexNameMap() {
        return headerIndexNameMap;
    }

    public void addHeaderIndex(int index, String name) {
        this.headerIndexNameMap.put(index, name);
    }

    public Map<Integer, String> getVariableIndexNameMap() {
        return variableIndexNameMap;
    }

    public void addVariableIndex(int index, String name) {
        this.variableIndexNameMap.put(index, name);
    }

    public int getBodyIndex() {
        return bodyIndex;
    }

    public void setBodyIndex(int bodyIndex) {
        this.bodyIndex = bodyIndex;
    }

    public boolean isResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(boolean responseEntity) {
        this.responseEntity = responseEntity;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MethodMetadata.class.getSimpleName() + "[", "]")
                .add("configKey='" + configKey + "'")
                .add("targetType=" + targetType)
                .add("method=" + method)
                .add("returnType=" + returnType)
                .add("publishType=" + publishType)
                .toString();
    }
}
