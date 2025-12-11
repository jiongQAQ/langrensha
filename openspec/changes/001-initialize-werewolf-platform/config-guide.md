# 项目配置文件指南

## 配置管理原则

✅ **统一配置文件管理**：API配置、模型配置统一在配置文件中管理
✅ **代码控制逻辑**：角色Prompt模板、策略逻辑在Java代码中定义
✅ **环境变量保护**：敏感信息（API Key）通过环境变量注入

---

## 1. application.yml 配置

### 完整配置示例

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

# ============================================
# LLM模型配置（核心配置）
# ============================================
llm:
  # API中转站配置（统一入口）
  proxy:
    enabled: true
    base-url: ${LLM_PROXY_URL:https://api.example.com/v1}
    api-key: ${LLM_API_KEY}
    timeout: 30000  # 毫秒

  # 重试配置
  retry:
    max-attempts: 3
    initial-delay: 1000  # 毫秒
    max-delay: 5000

  # 支持的模型列表
  models:
    - id: qwen-max
      name: "通义千问-Max"
      provider: qwen
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: true
      parameters:
        temperature: 0.7
        max-tokens: 2000
        top-p: 0.9

    - id: gpt-4
      name: "GPT-4"
      provider: openai
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: true
      parameters:
        temperature: 0.8
        max-tokens: 2000
        top-p: 1.0

    - id: gpt-3.5-turbo
      name: "GPT-3.5 Turbo"
      provider: openai
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: true
      parameters:
        temperature: 0.7
        max-tokens: 1500

    - id: claude-3-sonnet
      name: "Claude 3 Sonnet"
      provider: anthropic
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: true
      parameters:
        temperature: 0.7
        max-tokens: 2000

    - id: gemini-pro
      name: "Gemini Pro"
      provider: google
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: false  # 默认关闭，可按需启用
      parameters:
        temperature: 0.7
        max-tokens: 2000

# ============================================
# 游戏配置
# ============================================
game:
  # 超时配置（秒）
  timeout:
    ai-speech: 30        # AI发言生成超时
    human-action: 60     # 人类玩家操作超时
    night-phase: 120     # 夜晚阶段总超时
    day-phase: 300       # 白天阶段总超时

  # 房间配置
  room:
    max-rooms: 10                # 最大房间数
    max-players-per-room: 6      # 每个房间最大玩家数
    max-spectators-per-room: 10  # 每个房间最大观战者数

  # 默认AI配置
  ai:
    default-model: qwen-max      # 默认使用的模型
    enable-reasoning: true       # 启用推理引擎
    memory-limit: 100            # 记忆条目数量限制

# ============================================
# 日志配置
# ============================================
logging:
  level:
    root: INFO
    com.werewolf: DEBUG
    com.werewolf.llm: INFO       # LLM调用日志
    com.werewolf.agent: DEBUG     # Agent日志
  file:
    name: logs/werewolf-platform.log
    max-size: 10MB
    max-history: 7
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 2. 环境变量配置

### 开发环境 (.env.dev)

```bash
# LLM API配置
LLM_PROXY_URL=https://your-dev-proxy.com/v1
LLM_API_KEY=sk-dev-xxxxxxxxxxxxxx

# 数据库配置（H2内存数据库，无需配置）

# 其他配置
SPRING_PROFILES_ACTIVE=dev
```

### 生产环境 (.env.prod)

```bash
# LLM API配置
LLM_PROXY_URL=https://your-prod-proxy.com/v1
LLM_API_KEY=sk-prod-xxxxxxxxxxxxxx

# MySQL数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/werewolf?useSSL=true
SPRING_DATASOURCE_USERNAME=werewolf_user
SPRING_DATASOURCE_PASSWORD=your_secure_password

# 其他配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8021
```

### 加载环境变量

```bash
# Linux/Mac
export $(cat .env.dev | xargs)

# 或者在启动时指定
source .env.dev && mvn spring-boot:run
```

---

## 3. 配置文件读取（Java代码）

### 模型配置读取

```java
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {

    private Proxy proxy;
    private Retry retry;
    private List<ModelConfig> models;

    @Data
    public static class Proxy {
        private boolean enabled;
        private String baseUrl;
        private String apiKey;
        private int timeout;
    }

    @Data
    public static class Retry {
        private int maxAttempts;
        private long initialDelay;
        private long maxDelay;
    }

    @Data
    public static class ModelConfig {
        private String id;
        private String name;
        private String provider;
        private String endpoint;
        private String apiKey;
        private boolean enabled;
        private Map<String, Object> parameters;
    }

    // Getters and Setters
}
```

### 获取可用模型

```java
@Service
public class ModelService {

    @Autowired
    private LLMConfig llmConfig;

    public List<ModelConfig> getAvailableModels() {
        return llmConfig.getModels().stream()
            .filter(ModelConfig::isEnabled)
            .collect(Collectors.toList());
    }

    public ModelConfig getModelById(String modelId) {
        return llmConfig.getModels().stream()
            .filter(m -> m.getId().equals(modelId))
            .findFirst()
            .orElseThrow(() -> new ModelNotFoundException(modelId));
    }
}
```

---

## 4. Prompt模板（代码管理）

### ⚠️ 不使用外部文件，直接在代码中定义

```java
// src/main/java/com/werewolf/agent/strategy/role/SeerStrategy.java

public class SeerStrategy implements RoleStrategy {

    // 夜晚行动Prompt模板
    private static final String NIGHT_PROMPT_TEMPLATE = """
        你是狼人杀游戏中的预言家（Seer）。

        【角色信息】
        - 阵营：好人阵营
        - 技能：每晚可以查验一名玩家的身份

        【当前目标】
        选择今晚要查验的玩家

        【游戏状态】
        - 当前回合：第%d回合
        - 存活玩家：%s

        【你的已知信息】
        - 已验身份：%s

        请选择一名玩家ID进行查验。只返回玩家ID，不要解释。
        """;

    // 白天发言Prompt模板
    private static final String DAY_PROMPT_TEMPLATE = """
        你是狼人杀游戏中的预言家（Seer）。

        【当前目标】
        白天发言阶段

        【游戏状态】
        - 当前回合：第%d回合
        - 昨晚死亡：%s
        - 存活玩家：%s

        【你的已知信息】
        - 已验身份：%s

        【历史发言摘要】
        %s

        【推理建议】
        1. 谨慎透露验人信息，避免过早暴露
        2. 分析其他玩家发言寻找矛盾
        3. 识别假预言家的破绽
        4. 保护自己身份不被狼人发现

        请生成你的发言内容（100-200字），语气自然像真人玩家。
        """;

    @Override
    public String buildNightPrompt(GameContext context) {
        return String.format(NIGHT_PROMPT_TEMPLATE,
            context.getRound(),
            context.getAlivePlayersString(),
            context.getCheckedResultsString());
    }

    @Override
    public String buildDayPrompt(GameContext context) {
        return String.format(DAY_PROMPT_TEMPLATE,
            context.getRound(),
            context.getLastNightDeathsString(),
            context.getAlivePlayersString(),
            context.getCheckedResultsString(),
            context.getSpeechSummary());
    }
}
```

### 为什么在代码中管理Prompt？

✅ **版本控制友好**：Prompt和代码逻辑一起版本管理
✅ **类型安全**：编译时检查占位符
✅ **易于维护**：修改Prompt时可以同时看到相关逻辑
✅ **单元测试**：可以测试Prompt生成逻辑
✅ **避免文件依赖**：不需要在运行时读取外部文件

---

## 5. 启动和部署

### 本地开发启动

```bash
# 1. 设置环境变量
export LLM_PROXY_URL=https://your-proxy.com/v1
export LLM_API_KEY=your-api-key

# 2. 启动应用
mvn spring-boot:run

# 或者使用IDE运行 WerewolfApplication.main()
```

### 生产环境部署

```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 使用环境变量启动
java -jar target/werewolf-platform.jar \
  --spring.profiles.active=prod \
  --llm.proxy.base-url=${LLM_PROXY_URL} \
  --llm.proxy.api-key=${LLM_API_KEY}
```

### Docker部署（可选）

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/werewolf-platform.jar app.jar

ENV LLM_PROXY_URL=""
ENV LLM_API_KEY=""

EXPOSE 8021

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 6. 访问地址

启动成功后，可通过以下地址访问：

- **REST API**: http://localhost:8021/api
- **WebSocket**: ws://localhost:8021/ws/game
- **H2控制台**: http://localhost:8021/h2-console
- **Swagger文档**: http://localhost:8021/swagger-ui.html
- **健康检查**: http://localhost:8021/actuator/health

---

## 7. 配置最佳实践

### ✅ 推荐做法

1. **敏感信息用环境变量**：API Key、数据库密码等
2. **配置分环境管理**：dev、test、prod不同配置
3. **Prompt在代码中**：便于版本控制和维护
4. **模型配置可热更新**：修改配置后重启应用即可
5. **日志合理分级**：开发环境DEBUG，生产环境INFO

### ❌ 避免做法

1. ❌ 不要在代码中硬编码API Key
2. ❌ 不要把 `.env` 文件提交到Git
3. ❌ 不要在外部文件管理Prompt（难以维护）
4. ❌ 不要忽略配置文件的注释说明

---

## 8. API中转站配置示例

如果你使用API中转站，请按以下格式配置：

### 中转站信息模板

```yaml
llm:
  proxy:
    base-url: https://your-proxy-domain.com/v1
    api-key: sk-xxxxxxxxxxxxxxxxxxxxxx
```

### 常见中转站格式

```yaml
# OpenAI格式中转站
llm:
  proxy:
    base-url: https://api.openai-proxy.com/v1
    api-key: sk-proj-xxxxxxxxxxxxx

# 通用中转站
llm:
  proxy:
    base-url: https://api.example.com/v1/chat/completions
    api-key: your-unified-key
```

---

**注意**:
1. 首次部署前请修改所有默认密码和API密钥
2. `.env` 文件不应提交到版本控制系统
3. 生产环境建议使用Secret管理工具（如AWS Secrets Manager、K8s Secrets等）

---

**配置文件位置**：
- 主配置: `src/main/resources/application.yml`
- 环境配置: `src/main/resources/application-{profile}.yml`
- 敏感信息: `.env` 文件或系统环境变量
