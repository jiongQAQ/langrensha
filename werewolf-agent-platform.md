# 狼人杀多智能体游戏平台 - 技术方案

## 项目概述

基于AgentScope框架构建的狼人杀多智能体游戏平台，支持人类玩家与多个AI智能体（使用不同大模型）混合对战。

## 系统架构

### 1. 核心组件

#### 1.1 游戏主持人 (GameMaster Agent)
- **职责**：
  - 控制游戏流程（夜晚 ↔ 白天循环）
  - 管理游戏状态和玩家状态
  - 执行游戏规则判定
  - 广播游戏事件和结果
  - 处理玩家行动并返回反馈

- **实现要点**：
  ```java
  class GameMasterAgent extends ReActAgent {
      - GameState gameState
      - List<Player> players
      - RuleEngine ruleEngine
      - EventBroadcaster broadcaster

      methods:
      - initGame(playerList, roleConfig)
      - runNightPhase()
      - runDayPhase()
      - processPlayerAction(playerId, action)
      - checkWinCondition()
      - broadcastEvent(event)
  }
  ```

#### 1.2 玩家智能体 (Player Agent)

**AI玩家智能体**：
- **职责**：
  - 接收游戏信息（公开信息、私密信息）
  - 分析游戏局势
  - 根据角色执行策略（推理、欺骗、说服）
  - 生成发言内容
  - 做出投票决策
  - 使用角色技能

- **核心能力**：
  - **记忆系统**：使用 InMemoryMemory 存储历史信息
  - **推理能力**：分析玩家发言、投票行为
  - **角色扮演**：根据分配的角色调整策略
  - **情感表达**：模拟人类发言风格

- **实现要点**：
  ```java
  class AIPlayerAgent extends ReActAgent {
      - Role role  // 狼人、预言家等
      - PlayerMemory memory
      - StrategyEngine strategy
      - LLMModel model  // 支持多种模型

      methods:
      - receiveGameInfo(info)
      - analyzeGameState()
      - generateSpeech(context)
      - makeVote(candidates)
      - useSkill(target)
      - updateBelief(newInfo)
  }
  ```

**人类玩家接口**：
- **职责**：
  - 接收人类玩家输入
  - 转换为标准游戏动作
  - 展示游戏信息给人类玩家

- **实现要点**：
  ```java
  class HumanPlayerInterface {
      - String playerId
      - InputHandler inputHandler
      - OutputRenderer renderer

      methods:
      - displayGameState(state)
      - getPlayerSpeech() -> String
      - getPlayerVote(candidates) -> String
      - getSkillTarget() -> String
  }
  ```

#### 1.3 通信中枢 (MsgHub)
- 使用 AgentScope 的 MsgHub 实现消息广播
- 支持多种消息类型：
  - 全局广播（所有玩家可见）
  - 定向消息（特定玩家可见，如狼人队友通信）
  - 私密消息（仅自己可见，如预言家验人结果）

#### 1.4 角色系统 (Role System)
```java
interface Role {
    String getRoleName();
    Camp getCamp();  // 狼人阵营 or 好人阵营
    Skill getSkill();
    boolean canSpeak();
}

// 角色实现
class Werewolf implements Role { ... }
class Seer implements Role { ... }
class Witch implements Role { ... }
class Hunter implements Role { ... }
class Guard implements Role { ... }
class Villager implements Role { ... }
```

#### 1.5 游戏状态管理器
```java
class GameState {
    - int currentRound
    - Phase currentPhase  // NIGHT / DAY
    - List<Player> alivePlayers
    - List<Player> deadPlayers
    - String sheriffId  // 警长
    - Map<String, RoleState> roleStates
    - VoteResult lastVoteResult
    - List<GameEvent> eventHistory
}
```

### 2. 游戏流程设计

#### 2.1 游戏初始化
```
1. 创建GameMaster
2. 配置游戏人数和板子（如：12人预女猎守）
3. 初始化AI玩家（不同模型）
4. 初始化人类玩家接口
5. 分配角色
6. 通知每个玩家自己的角色
7. 开始第一晚
```

#### 2.2 夜晚流程
```
GameMaster 控制流程：
1. 宣布天黑请闭眼
2. 按顺序激活角色技能：
   a. 狼人睁眼 → 狼人队内讨论 → 选择击杀目标
   b. 预言家睁眼 → 选择验人 → 收到验人结果
   c. 女巫睁眼 → 查看死亡信息 → 决定是否用药
   d. 守卫睁眼 → 选择守护目标
3. 计算夜晚结果（考虑守护、解药等）
4. 进入白天
```

#### 2.3 白天流程
```
1. 公布昨晚死亡信息
2. 死者发表遗言（如果有）
3. 警长竞选（第一天）或警长指定发言顺序
4. 玩家依次发言：
   - AI玩家：调用模型生成发言
   - 人类玩家：等待输入
5. 投票环节：
   - 每个玩家选择投票目标
   - 统计投票结果
6. 放逐得票最多的玩家
7. 被放逐玩家发表遗言
8. 检查胜利条件
9. 进入下一晚
```

### 3. 多模型集成方案

#### 3.1 模型抽象层
```java
interface LLMModelAdapter {
    String generateResponse(String prompt, Map<String, Object> context);
    boolean isAvailable();
}

// 具体实现
class QwenModelAdapter implements LLMModelAdapter { ... }
class GPTModelAdapter implements LLMModelAdapter { ... }
class ClaudeModelAdapter implements LLMModelAdapter { ... }
class GeminiModelAdapter implements LLMModelAdapter { ... }
class LocalModelAdapter implements LLMModelAdapter { ... }
```

#### 3.2 模型配置
```java
class ModelConfig {
    String modelName;
    String apiKey;
    String endpoint;
    Map<String, String> parameters;  // temperature, max_tokens 等
    String personalityPrompt;  // 玩家性格设定
}
```

#### 3.3 玩家配置示例
```java
// 12人局配置：11个AI + 1个人类
List<PlayerConfig> configs = List.of(
    new PlayerConfig("Player1", ModelType.HUMAN, null),
    new PlayerConfig("Player2", ModelType.QWEN_MAX, qwenConfig),
    new PlayerConfig("Player3", ModelType.GPT4, gptConfig),
    new PlayerConfig("Player4", ModelType.CLAUDE, claudeConfig),
    new PlayerConfig("Player5", ModelType.GEMINI, geminiConfig),
    new PlayerConfig("Player6", ModelType.QWEN_MAX, qwenConfig2),
    // ... 更多玩家
);
```

### 4. 人类玩家参与机制

#### 4.1 输入方式
- **命令行界面**（CLI）
- **Web界面**（推荐）：使用 WebSocket 实时通信
- **桌面应用**：JavaFX/Swing

#### 4.2 人类玩家交互流程
```
1. 接收游戏状态更新
2. 当轮到人类玩家行动时：
   - 暂停游戏流程
   - 显示当前状态和可选操作
   - 等待玩家输入（设置超时）
   - 验证输入合法性
   - 提交行动
3. 继续游戏流程
```

#### 4.3 实时信息展示
```
游戏界面包含：
- 玩家列表及存活状态
- 当前回合和阶段
- 历史发言记录
- 投票历史
- 自己的角色信息
- 可执行操作按钮
```

### 5. 智能体策略设计

#### 5.1 Prompt 工程
为不同角色设计专门的系统提示：

**预言家Prompt示例**：
```
你是狼人杀游戏中的预言家。
- 你的目标是帮助好人找出所有狼人。
- 你每晚可以验证一名玩家的身份。
- 你需要巧妙地透露验人信息，但不能过早暴露自己。
- 分析其他玩家的发言，识别谁可能是狼人。
- 当前已验身份：{已验玩家信息}
- 其他玩家发言历史：{发言记录}
请根据当前局势发言。
```

**狼人Prompt示例**：
```
你是狼人杀游戏中的狼人。
- 你的目标是杀光所有好人阵营玩家。
- 你的狼队友是：{队友列表}
- 白天你需要伪装成好人，误导其他玩家。
- 注意保护队友，必要时可以牺牲自己。
- 分析预言家、女巫等神职位置。
- 当前局势：{游戏状态}
请生成你的发言，记住要隐藏你的狼人身份。
```

#### 5.2 记忆管理
```java
class PlayerMemory {
    - List<SpeechRecord> allSpeeches  // 所有发言
    - Map<String, PlayerBelief> beliefs  // 对每个玩家的判断
    - List<VoteRecord> voteHistory  // 投票历史
    - List<DeathRecord> deathRecords  // 死亡记录
    - PrivateInfo privateInfo  // 私密信息（如验人结果）

    methods:
    - addSpeech(playerId, content)
    - updateBelief(playerId, role, confidence)
    - analyzePattern()  // 分析投票模式、发言模式
}
```

#### 5.3 推理引擎
```java
class ReasoningEngine {
    - analyzeVotingPattern()  // 分析投票模式找狼人
    - detectContradiction()  // 检测发言矛盾
    - identifyJumpingRoles()  // 识别跳神（多人自称同一角色）
    - calculateSuspicion()  // 计算每个玩家的可疑度
    - generateArgument()  // 生成论证逻辑
}
```

### 6. 技术栈

#### 6.1 核心框架
- **AgentScope (Java)**: 多智能体框架
- **Spring Boot**: Web服务（如果需要Web界面）
- **WebSocket**: 实时通信

#### 6.2 大模型集成
- **阿里云DashScope**: Qwen系列
- **OpenAI API**: GPT系列
- **Anthropic API**: Claude系列
- **Google AI**: Gemini系列
- **本地模型**: Ollama/LMStudio

#### 6.3 存储
- **内存数据库**: H2/Redis（游戏状态）
- **持久化**: MySQL/PostgreSQL（游戏历史记录）

#### 6.4 前端（可选）
- **Vue.js/React**: Web界面
- **Socket.io**: 实时通信

### 7. 项目结构

```
werewolf-agent-platform/
├── src/main/java/com/werewolf/
│   ├── agent/
│   │   ├── GameMasterAgent.java
│   │   ├── AIPlayerAgent.java
│   │   └── HumanPlayerInterface.java
│   ├── role/
│   │   ├── Role.java
│   │   ├── Werewolf.java
│   │   ├── Seer.java
│   │   ├── Witch.java
│   │   └── ...
│   ├── model/
│   │   ├── LLMModelAdapter.java
│   │   ├── QwenModelAdapter.java
│   │   ├── GPTModelAdapter.java
│   │   └── ...
│   ├── game/
│   │   ├── GameState.java
│   │   ├── RuleEngine.java
│   │   ├── VoteManager.java
│   │   └── EventBroadcaster.java
│   ├── strategy/
│   │   ├── PromptBuilder.java
│   │   ├── ReasoningEngine.java
│   │   └── MemoryManager.java
│   ├── communication/
│   │   ├── MsgHubWrapper.java
│   │   └── MessageType.java
│   └── config/
│       ├── GameConfig.java
│       └── ModelConfig.java
├── src/main/resources/
│   ├── prompts/
│   │   ├── werewolf.txt
│   │   ├── seer.txt
│   │   └── ...
│   └── application.yml
├── frontend/ (可选Web界面)
│   ├── src/
│   ├── public/
│   └── package.json
├── pom.xml
└── README.md
```

### 8. 实现路线图

#### Phase 1: 核心游戏引擎
- [ ] 实现GameMaster和基础游戏流程
- [ ] 实现角色系统和技能机制
- [ ] 实现规则引擎和胜利条件判定
- [ ] 命令行版本的人类玩家接口

#### Phase 2: 智能体集成
- [ ] 集成AgentScope框架
- [ ] 实现AIPlayerAgent基础框架
- [ ] 设计并测试不同角色的Prompt
- [ ] 实现记忆和推理系统

#### Phase 3: 多模型支持
- [ ] 实现模型适配器接口
- [ ] 集成Qwen、GPT、Claude等模型
- [ ] 实现模型配置和切换机制
- [ ] 性能优化和并发调用

#### Phase 4: 用户体验
- [ ] 开发Web界面
- [ ] 实现WebSocket实时通信
- [ ] 游戏回放功能
- [ ] 数据统计和可视化

#### Phase 5: 高级功能
- [ ] 更多角色支持（白狼王、守墓人等）
- [ ] 多种板子配置
- [ ] 智能体性格定制
- [ ] 游戏复盘和AI策略分析

### 9. 关键挑战与解决方案

#### 9.1 AI玩家推理能力
**挑战**: 狼人杀需要复杂的逻辑推理和欺骗能力
**解决方案**:
- 使用高性能模型（GPT-4、Claude Opus、Qwen-Max）
- 设计详细的Prompt，包含推理框架
- 提供完整的游戏历史上下文
- 实现记忆检索和模式识别

#### 9.2 实时性要求
**挑战**: 等待AI生成回复会影响游戏体验
**解决方案**:
- 并行调用多个模型API
- 设置合理的超时时间
- 提供"快速模式"和"深思模式"选项
- 使用流式输出展示AI思考过程

#### 9.3 模型成本控制
**挑战**: 频繁调用大模型API成本较高
**解决方案**:
- 混合使用不同价位的模型
- 优化Prompt长度，减少token消耗
- 使用本地模型作为部分玩家
- 实现对话摘要和上下文压缩

#### 9.4 游戏公平性
**挑战**: 不同模型能力差异可能影响游戏平衡
**解决方案**:
- 为弱模型提供更详细的提示
- 随机分配模型到不同角色
- 收集数据分析模型表现
- 动态调整模型参数平衡能力

### 10. 扩展可能性

- **多人在线对战**: 支持12个人类玩家联机
- **观战模式**: 允许观众观看AI对战
- **训练模式**: 新手可以和AI学习狼人杀
- **竞技模式**: AI策略锦标赛
- **语音支持**: 集成TTS和STT实现语音游戏
- **多语言支持**: 英文、日文等语言版本

## 开发建议

1. **先实现最小可用版本 (MVP)**:
   - 6人简化局（2狼2民1预1女）
   - 只支持1个模型
   - 命令行界面

2. **逐步扩展**:
   - 增加到12人标准局
   - 添加多模型支持
   - 开发Web界面

3. **测试策略**:
   - 单元测试各个组件
   - 集成测试游戏流程
   - 人工测试AI表现
   - 压力测试并发性能

## 参考资源

- AgentScope文档: https://java.agentscope.io/
- 狼人杀规则: https://www.langrensha.net/strategy/2023051601.html
- 多智能体辩论: https://java.agentscope.io/zh/multi-agent/multiagent-debate.html

---

**预计开发周期**:
- MVP版本: 2-3周
- 完整版本: 2-3个月

**技术难度**: ⭐⭐⭐⭐☆

**创新点**:
- 首个基于多大模型的狼人杀AI对战平台
- 人机混合游戏体验
- 可用于研究大模型推理和欺骗能力
