# 项目配置文件示例

## application.yml 配置

```yaml
# 服务器配置
server:
  port: 8021  # 使用8021端口，避免与其他Java应用冲突

spring:
  application:
    name: werewolf-platform

  # 数据库配置（开发环境使用H2）
  datasource:
    url: jdbc:h2:mem:werewolf
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  # JPA配置
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# WebSocket配置
websocket:
  endpoint: /ws/game
  allowed-origins: "*"

# LLM API配置（使用环境变量）
llm:
  # API中转站配置
  proxy:
    base-url: ${LLM_PROXY_URL:https://api.example.com}
    api-key: ${LLM_API_KEY:your-api-key}

  # 模型超时配置（秒）
  timeout: 30

  # 重试配置
  retry:
    max-attempts: 3
    delay: 1000  # 毫秒

# 游戏配置
game:
  # AI发言超时（秒）
  ai-speech-timeout: 30
  # 人类玩家超时（秒）
  human-action-timeout: 60
  # 最大房间数
  max-rooms: 10
  # 每个房间最大玩家数
  max-players-per-room: 6

# 日志配置
logging:
  level:
    root: INFO
    com.werewolf: DEBUG
  file:
    name: logs/werewolf-platform.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 环境变量配置

在生产环境中，敏感信息应该通过环境变量配置：

```bash
# LLM API配置
export LLM_PROXY_URL=https://your-api-proxy.com
export LLM_API_KEY=your-actual-api-key

# 数据库配置（生产环境使用MySQL）
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/werewolf
export SPRING_DATASOURCE_USERNAME=werewolf_user
export SPRING_DATASOURCE_PASSWORD=secure_password
```

## 启动命令

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
```bash
java -jar werewolf-platform.jar --spring.profiles.active=prod
```

## 访问地址

- **API Base URL**: http://localhost:8021/api
- **WebSocket**: ws://localhost:8021/ws/game
- **H2 Console**: http://localhost:8021/h2-console
- **Swagger UI**: http://localhost:8021/swagger-ui.html

## LLM API中转站配置示例

如果你使用API中转站，请提供以下信息：

1. **Base URL**: 中转站的基础URL
2. **API Key**: 认证密钥
3. **支持的模型列表**: 可用的模型名称
4. **请求格式**: 是否兼容OpenAI格式

示例：
```yaml
llm:
  proxy:
    base-url: https://api.your-proxy.com/v1
    api-key: sk-xxxxxxxxxxxxx
  models:
    - name: gpt-4
      type: openai
    - name: qwen-max
      type: qwen
    - name: claude-3-sonnet
      type: claude
```

---

**注意**: 请在实际部署时修改所有默认密码和API密钥！
