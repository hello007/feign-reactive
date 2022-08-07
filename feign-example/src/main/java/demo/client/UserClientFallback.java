package demo.client;

import demo.entity.UserInfo;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:43
 */
public class UserClientFallback implements UserClient {
    @Override
    public Mono<Map<String, Object>> queryUserInfo(String userId) {
        return Mono.just(new HashMap<>(1));
    }

    @Override
    public Mono<Map<String, Object>> insertUserInfo(UserInfo userInfo) {
        return Mono.just(new HashMap<>(1));
    }
}
