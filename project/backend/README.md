# 🐺 狼人杀多智能体游戏平台

基于AgentScope和多种大语言模型的智能狼人杀游戏平台，支持人类玩家与AI智能体混合对战。

## ✨ 特性

- 🎮 **6人局狼人杀**：2狼2民1预1女标准配置
- 🤖 **多模型支持**：Qwen、GPT、Claude、Gemini等
- 🌐 **Web实时对战**：WebSocket实时通信
- 👁️ **观战模式**：上帝视角观看AI对局
- 🧠 **智能推理**：AI具备逻辑推理和策略能力

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- LLM API访问（通过中转站）

### 环境配置

1. **设置环境变量**

```bash
export LLM_PROXY_URL=https://your-proxy-url.com/v1
export LLM_API_KEY=your-api-key
```

或创建 `.env` 文件：

```bash
LLM_PROXY_URL=https://your-proxy-url.com/v1
LLM_API_KEY=your-api-key
```

2. **编译项目**

```bash
mvn clean compile
```

3. **启动应用**

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8021 启动。

### 访问地址

- **REST API**: http://localhost:8021/api
- **Swagger文档**: http://localhost:8021/swagger-ui.html
- **H2控制台**: http://localhost:8021/h2-console
- **WebSocket**: ws://localhost:8021/ws/game

## 📋 开发进度

### Phase 1: 核心游戏引擎 (当前)
- [x] TASK-001: 创建Maven项目结构
- [x] TASK-002: 配置Spring Boot开发环境
- [ ] TASK-003: 实现角色系统
- [ ] TASK-004: 实现游戏状态模型
- [ ] TASK-005: 实现夜晚流程引擎
- [ ] TASK-006: 实现白天流程引擎
- [ ] TASK-007: 实现胜利条件判定
- [ ] TASK-008: 实现游戏主控制器
- [ ] TASK-009: 编写核心引擎测试

### Phase 2-6: 待开发
详见 `openspec/changes/001-initialize-werewolf-platform/tasks.md`

## 🏗️ 项目结构

```
werewolf-platform/
├── src/main/java/com/werewolf/
│   ├── WerewolfApplication.java      # Spring Boot主类
│   ├── domain/                       # 领域模型
│   │   ├── role/                     # 角色系统
│   │   └── model/                    # 游戏实体
│   ├── engine/                       # 游戏引擎
│   │   ├── night/                    # 夜晚流程
│   │   └── day/                      # 白天流程
│   ├── agent/                        # 智能体系统
│   │   ├── strategy/                 # AI策略
│   │   ├── memory/                   # 记忆管理
│   │   └── communication/            # 通信机制
│   ├── llm/                          # LLM集成
│   │   └── adapter/                  # 模型适配器
│   ├── service/                      # 业务服务
│   └── web/                          # Web层
│       ├── controller/               # REST API
│       └── websocket/                # WebSocket
├── src/main/resources/
│   └── application.yml               # 配置文件
├── src/test/java/                    # 测试代码
├── openspec/                         # OpenSpec规范文档
│   ├── AGENTS.md
│   └── changes/001-initialize-werewolf-platform/
│       ├── proposal.md               # 项目提案
│       ├── spec-delta.md             # 详细规范 (62条需求)
│       ├── tasks.md                  # 任务清单 (57个任务)
│       └── config-guide.md           # 配置指南
└── pom.xml                           # Maven配置
```

## 🎯 游戏规则

### 角色配置（6人局）
- **狼人** x2：夜晚击杀一名玩家
- **平民** x2：无特殊技能
- **预言家** x1：每晚查验一名玩家身份
- **女巫** x1：拥有解药（救人）和毒药（杀人）各一次

### 胜利条件
- **好人胜**：所有狼人被放逐
- **狼人胜**：所有神职或所有平民死亡

## 📖 文档

- [项目提案](openspec/changes/001-initialize-werewolf-platform/proposal.md)
- [详细规范](openspec/changes/001-initialize-werewolf-platform/spec-delta.md)
- [任务清单](openspec/changes/001-initialize-werewolf-platform/tasks.md)
- [配置指南](openspec/changes/001-initialize-werewolf-platform/config-guide.md)

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=GameEngineTest
```

## 🔧 开发

### 添加新角色

1. 在 `domain/role/` 创建角色类
2. 在 `agent/strategy/role/` 实现策略类
3. 在策略类中定义Prompt模板
4. 在 `engine/` 中添加角色技能处理逻辑

### 添加新LLM模型

在 `application.yml` 中添加模型配置：

```yaml
llm:
  models:
    - id: your-model
      name: "Your Model"
      provider: your-provider
      endpoint: ${llm.proxy.base-url}
      api-key: ${llm.proxy.api-key}
      enabled: true
```

## 🤝 贡献

基于 **OpenSpec** 工作流开发：

1. 在 `openspec/changes/` 创建新的变更提案
2. 编写 `proposal.md`、`spec-delta.md`、`tasks.md`
3. 获得批准后开始实现
4. 完成后归档到 `openspec/archived/`

## 📝 License

MIT License

## 👥 作者

- User (项目发起人)
- Claude Sonnet 4.5 (AI开发助手)

---

**当前版本**: v0.1.0-SNAPSHOT
**开发状态**: Phase 1 - 核心游戏引擎开发中
**最后更新**: 2025-12-11
