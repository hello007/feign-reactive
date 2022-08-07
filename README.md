# Feign-Reactive

参考[Playtika feign-reactive](https://github.com/PlaytikaOSS/feign-reactive) ，引用了很多核心代码，进行了逻辑简化，暂不支持注册中心的自动路由发现机制，适合小项目进行魔改。

仅供参考，自我学习使用。

## 代码

见feign-core模块

## 流程图
![流程调用图](http://processon.com/chart_image/62e333317d9c08072e68c9f3.png)


# Feign使用

## 使用

1. 注入fegin.config.ReactiveFeignAutoConfiguration（import或spring.factories等方式均可）
2. 启动类标记 @EnableReactiveFeignClients 注解
3. 自定义接口，标记 @ReactiveFeignClient 注解

## 常用类

### @EnableReactiveFeignClients

开启Feign并自动扫描所有的 @ReactiveFeignClient 注解的bean并自动代理

#### 注解含义

`value`：扫描ReactiveFeignClient的根package集合

`basePackages`： 同value

`basePackageClasses`： 扫描ReactiveFeignClient标记的类所在的package集合

`clients`: 所有要加载的ReactiveFeignClient标记的所有类，如果不为空，则忽略上述package集合

`defaultConfiguration`： 默认配置configuration集合，会将其标记的所有Bean对象注入到所有FeignClient内

### @ReactiveFeignClient

标记FeignClient类，该类必须为接口。

#### 注解含义

`value/name`： 当前FeignClient名，唯一标识，用于隔离不同的configuration

`url`： 要访问的url路径，域名或实际url（不支持注册中心方式）

`configuration`： feign client自定义的配置类，如注入自定义Interceptor、fallback等

`fallback`： 失败回调类名，为当前@ReactiveFeignClient标记的接口的实现类

`fallbackFactory`： fallback工厂类，需实现 `fegin.fallback.FallbackFactory` 接口

`path`： 内所有方法请求的统一前缀路径

### ReactiveHttpRequestInterceptor

Request拦截器，可以拦截ClientRequest对象注入自定义信息

### AbstractHttpRequestInterceptor

默认ReactiveHttpRequestInterceptor抽象实现类，通过构造函数传入order级别进行优先级排序

### ReactiveHttpHeaderInterceptor

自定义HttpHeader拦截器，方便注入自定义header值

### ReactiveHttpResponseInterceptor

Response拦截器，可以拦截ClientResponse对象注入自定义信息

### AbstractHttpResponseInterceptor

默认ReactiveHttpResponseInterceptor抽象实现类，通过构造函数传入order级别进行优先级排序

### ClientOptionDefinition

WebClient可选配置定义类。

常用选项为：

fegin.domain.ClientOption#READ_TIMEOUT_OPTION： 读超时时间，ms

fegin.domain.ClientOption#WRITE_TIMEOUT_OPTION： 写超时时间，ms

io.netty.channel.ChannelOption#CONNECT_TIMEOUT_MILLIS： 连接超时时间，ms



## 示例

见 feign-example 中的 `demo.client.UserClient` 

