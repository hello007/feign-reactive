package demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuyang
 * 创建时间: 2022-08-07 18:32
 */
@RestController
public class DemoController {

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/query")
    public Mono<Map<String, Object>> queryUserInfo(ServerHttpRequest request, @RequestParam String userId) {
        Map<String, String> params = request.getQueryParams().toSingleValueMap();
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", userId);
        map.put("name", "liu");
        map.put("userAddress", "北京");
        map.put("请求参数", params);
        map.put("header", request.getHeaders());
        return Mono.just(map);
    }

    @PostMapping("/insert")
    public Mono<Map<String, Object>> insertUserInfo(ServerHttpRequest request, @RequestBody UserInfo userInfo) {
        Map<String, Object> map = objectMapper.convertValue(userInfo, new TypeReference<Map<String, Object>>() {
        });
        map.put("header", request.getHeaders());
        map.put("from", DemoController.class.getName());
        return Mono.just(map);
    }
}
