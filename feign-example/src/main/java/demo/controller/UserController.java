package demo.controller;

import demo.client.UserClient;
import demo.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:45
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserClient userClient;

    @GetMapping("/query")
    public Mono<Map<String, Object>> queryUserInfo(@RequestParam("userId") String userId) {
        return userClient.queryUserInfo(userId);
    }

    @PostMapping("/insert")
    public Mono<Map<String, Object>> insertUserInfo(@RequestBody UserInfo userInfo) {
        return userClient.insertUserInfo(userInfo);
    }

}
