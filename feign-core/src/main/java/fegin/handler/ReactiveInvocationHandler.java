package fegin.handler;

import fegin.domain.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 自定义Feign代理类handler
 *
 * @author liuyang
 * 创建时间: 2022-07-23 21:03
 */
public class ReactiveInvocationHandler implements InvocationHandler {

    private Target target;

    private Map<Method, MethodHandler> dispatch;

    public ReactiveInvocationHandler(Target target, Map<Method, MethodHandler> dispatch) {
        this.target = target;
        this.dispatch = dispatch;
        defineObjectMethodsHandlers();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return dispatch.get(method).invoke(args);
    }

    private void defineObjectMethodsHandlers() {
        try {
            dispatch.put(Object.class.getMethod("equals", Object.class),
                    args -> {
                        Object otherHandler = args.length > 0 && args[0] != null
                                ? Proxy.getInvocationHandler(args[0])
                                : null;
                        return equals(otherHandler);
                    });
            dispatch.put(Object.class.getMethod("hashCode"),
                    args -> hashCode());
            dispatch.put(Object.class.getMethod("toString"),
                    args -> toString());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ReactiveInvocationHandler) {
            final ReactiveInvocationHandler otherHandler = (ReactiveInvocationHandler) other;
            return this.target.equals(otherHandler.target);
        }
        return false;
    }

}
