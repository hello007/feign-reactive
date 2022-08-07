package fegin.handler;

/**
 * 自定义方法处理器接口
 *
 * @author liuyang
 * 创建时间: 2022-07-23 20:57
 */
public interface MethodHandler {

    /**
     * 方法执行
     *
     * @param argv 执行方法所需要的参数
     * @return 方法的执行结果
     * @throws Throwable 异常
     */
    Object invoke(Object[] argv) throws Throwable;
}
