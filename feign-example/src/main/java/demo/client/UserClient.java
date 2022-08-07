package demo.client;

import demo.entity.UserInfo;
import fegin.annotations.ReactiveFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:38
 */
@ReactiveFeignClient(name = "UserClient", url = "${url.user}", configuration = UserConfig.class)
public interface UserClient {

    @GetMapping("/query")
    Mono<Map<String, Object>> queryUserInfo(@RequestParam("userId") String userId);

    @PostMapping("/insert")
    Mono<Map<String, Object>> insertUserInfo(@RequestBody UserInfo userInfo);
}
