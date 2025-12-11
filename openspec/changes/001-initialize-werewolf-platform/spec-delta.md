# Spec Delta: Werewolf Multi-Agent Platform

## Change Metadata

- **Change ID**: 001-initialize-werewolf-platform
- **Type**: NEW_FEATURE
- **Impact**: HIGH (新系统)
- **Version**: v0.1.0-alpha

---

## ADDED Requirements

### 1. 游戏核心系统 (GAME_CORE)

#### 1.1 游戏配置 (GAME_CORE.CONFIG)

**REQ-001**: 系统必须支持6人狼人杀游戏配置
- 2名狼人 (Werewolf)
- 2名平民 (Villager)
- 1名预言家 (Seer)
- 1名女巫 (Witch)

**REQ-002**: 游戏必须按固定流程循环执行
- 流程：首夜 → 白天1 → 夜晚2 → 白天2 → ... 直到游戏结束
- 首夜：仅执行角色技能，不公布死亡信息
- 常规夜晚：狼人 → 预言家 → 女巫 → 守卫（未来扩展）
- 白天：公布死讯 → 遗言 → 发言 → 投票 → 放逐

#### 1.2 角色系统 (GAME_CORE.ROLES)

**REQ-003**: 狼人角色规范
- 阵营：狼人阵营
- 夜晚技能：与其他狼人队友协商后，选择一名玩家击杀
- 队友可见：可以看到其他狼人身份
- 发言策略：白天伪装好人身份

**REQ-004**: 预言家角色规范
- 阵营：好人阵营（神职）
- 夜晚技能：选择一名玩家查验身份（返回"狼人"或"好人"）
- 每晚可查验一次，无使用次数限制
- 发言策略：需谨慎透露验人信息

**REQ-005**: 女巫角色规范
- 阵营：好人阵营（神职）
- 夜晚技能：
  - 解药：可救活当晚被狼人杀死的玩家（全局仅1次）
  - 毒药：可毒死任意一名玩家（全局仅1次）
  - 限制：解药和毒药不能在同一夜使用
- 特殊规则：女巫首夜不能自救

**REQ-006**: 平民角色规范
- 阵营：好人阵营
- 无特殊技能
- 依靠发言分析和投票帮助好人阵营

#### 1.3 游戏规则引擎 (GAME_CORE.RULES)

**REQ-007**: 夜晚行动顺序
```
1. 狼人睁眼
   - 显示队友身份
   - 队内讨论（AI智能体间私聊）
   - 投票选择击杀目标
   - 记录击杀目标

2. 预言家睁眼
   - 选择查验目标
   - 获得查验结果（狼人/好人）

3. 女巫睁眼
   - 显示今晚死亡信息
   - 选择是否使用解药
   - 选择是否使用毒药
   - 记录药品使用

4. 计算最终死亡名单
   - 基础：狼人击杀目标
   - 救活：女巫解药 → 目标复活
   - 额外：女巫毒药 → 新增死亡
```

**REQ-008**: 白天流程
```
1. 公布死讯
   - 宣布昨晚死亡玩家
   - 不公布死亡原因（狼刀/毒药）

2. 遗言环节（按死亡顺序）
   - 每位死亡玩家有60秒发表遗言
   - 遗言后立即出局，不再参与游戏

3. 发言环节
   - 存活玩家依次发言（顺序：随机或按座位号）
   - 每人发言时间限制：60秒（人类）/ 30秒（AI）
   - 发言内容：分享信息、逻辑推理、投票意向

4. 投票环节
   - 所有存活玩家同时投票
   - 投票对象：任意存活玩家（可弃票）
   - 投票时限：60秒（人类）/ 10秒（AI）

5. 放逐结果
   - 统计票数，票数最多者被放逐
   - 平票：当日无人出局（简化规则）
   - 被放逐玩家可发表最后遗言（30秒）
```

**REQ-009**: 胜利条件判定
- 狼人胜利：所有好人阵营神职死亡 **或** 所有平民死亡
- 好人胜利：所有狼人死亡
- 每个回合结束后立即检查胜利条件

#### 1.4 游戏状态管理 (GAME_CORE.STATE)

**REQ-010**: 游戏状态必须包含以下信息
```java
GameState {
    String roomId;                          // 房间ID
    int roundNumber;                        // 当前回合数（从1开始）
    Phase currentPhase;                     // 当前阶段：NIGHT / DAY
    List<Player> players;                   // 所有玩家列表
    Map<String, Boolean> aliveStatus;       // 玩家存活状态
    Map<String, Role> roleAssignments;      // 玩家角色分配
    NightActionRecord nightActions;         // 夜晚行动记录
    VoteRecord lastVoteRecord;              // 上一轮投票记录
    List<GameEvent> eventHistory;           // 游戏事件历史
    WinCondition winCondition;              // 胜利条件（null表示游戏进行中）
}
```

**REQ-011**: 游戏事件必须完整记录
- 事件类型：ROLE_ASSIGNED, NIGHT_ACTION, DAY_SPEECH, VOTE_CAST, PLAYER_DIED, GAME_END
- 每个事件包含：时间戳、类型、相关玩家、详细数据
- 事件按时间顺序存储

---

### 2. 多智能体系统 (AGENT_SYSTEM)

#### 2.1 GameMaster Agent (AGENT_SYSTEM.GAMEMASTER)

**REQ-012**: GameMaster 职责定义
- 初始化游戏（分配角色、设置初始状态）
- 控制游戏流程（夜晚/白天切换）
- 执行游戏规则（技能生效、投票统计、胜负判定）
- 广播游戏事件（通过 MsgHub）
- 处理玩家行动请求
- 维护游戏状态一致性

**REQ-013**: GameMaster 必须使用 AgentScope 的 ReActAgent
```java
class GameMasterAgent extends ReActAgent {
    private GameState gameState;
    private RuleEngine ruleEngine;
    private MsgHub msgHub;

    // 核心方法
    void initializeGame(GameConfig config);
    void startGame();
    void processPlayerAction(PlayerAction action);
    void runNightPhase();
    void runDayPhase();
    WinCondition checkWinCondition();
    void broadcastEvent(GameEvent event);
    void endGame(WinCondition result);
}
```

#### 2.2 AI Player Agent (AGENT_SYSTEM.AIPLAYER)

**REQ-014**: AIPlayer 基础架构
- 必须继承 AgentScope 的 ReActAgent
- 每个 AIPlayer 代表一个AI玩家
- 必须支持不同的 LLM 模型
- 必须有独立的记忆系统

**REQ-015**: AIPlayer 核心能力
```java
class AIPlayerAgent extends ReActAgent {
    private String playerId;
    private Role assignedRole;
    private LLMModelAdapter modelAdapter;
    private PlayerMemory memory;
    private PromptBuilder promptBuilder;

    // 核心方法
    void receiveRole(Role role);                    // 接收角色分配
    void receiveGameInfo(GameInfo info);            // 接收游戏信息
    String generateSpeech(SpeechContext context);   // 生成发言
    String makeVote(List<Player> candidates);       // 投票决策
    String useSkill(SkillContext context);          // 使用技能
    void updateBelief(GameEvent event);             // 更新推理状态
}
```

**REQ-016**: 记忆系统规范
- 存储所有玩家历史发言
- 存储投票记录
- 存储已知信息（如预言家的验人结果）
- 维护对每个玩家的"怀疑度"评分
- 支持记忆检索和模式识别

#### 2.3 MsgHub 通信机制 (AGENT_SYSTEM.MSGHUB)

**REQ-017**: 消息类型定义
- **全局消息** (BROADCAST): 所有玩家可见（如白天发言、投票结果）
- **私密消息** (PRIVATE): 仅特定玩家可见（如预言家验人结果）
- **队伍消息** (TEAM): 仅队友可见（如狼人夜间讨论）
- **系统消息** (SYSTEM): 游戏流程控制消息

**REQ-018**: MsgHub 必须支持消息过滤
- 根据玩家身份过滤可见消息
- 死亡玩家不再接收游戏消息（观战除外）
- 保证信息对称性（按游戏规则）

---

### 3. LLM 模型集成 (LLM_INTEGRATION)

#### 3.1 模型适配器接口 (LLM_INTEGRATION.ADAPTER)

**REQ-019**: 统一的 LLM 适配器接口
```java
interface LLMModelAdapter {
    String getModelName();
    String generateResponse(String prompt, ModelConfig config);
    boolean isAvailable();
    void configure(Map<String, String> settings);
}
```

**REQ-020**: 模型配置支持
- 支持 API 中转站配置（baseURL 自定义）
- 支持 API Key 配置
- 支持模型参数配置（temperature, max_tokens, top_p等）
- 支持超时设置（默认30秒）

#### 3.2 支持的模型 (LLM_INTEGRATION.MODELS)

**REQ-021**: 必须支持以下模型类型
1. **OpenAI 兼容接口**
   - GPT-3.5-turbo
   - GPT-4
   - GPT-4-turbo
   - 通过中转站的其他 OpenAI 兼容模型

2. **阿里云通义千问**
   - Qwen-Max
   - Qwen-Plus
   - Qwen-Turbo

3. **Anthropic Claude**
   - Claude-3-Sonnet
   - Claude-3-Opus
   - Claude-3-Haiku

4. **Google Gemini**
   - Gemini-Pro

**REQ-022**: 模型适配器实现要求
- 实现重试机制（最多3次）
- 实现错误处理和降级策略
- 记录API调用日志
- 支持流式输出（可选）

#### 3.3 Prompt 工程 (LLM_INTEGRATION.PROMPT)

**REQ-023**: 角色特定 Prompt 模板
- 为每个角色设计专门的系统提示词
- Prompt 必须包含：角色介绍、目标、策略建议、游戏状态、历史信息
- 根据游戏阶段动态生成 Prompt（夜晚行动 vs 白天发言）

**REQ-024**: Prompt 模板示例 - 预言家
```
你是狼人杀游戏中的预言家（Seer）。

【角色信息】
- 阵营：好人阵营
- 技能：每晚可以查验一名玩家的身份

【当前目标】
{当前阶段目标：如"选择今晚要查验的玩家"或"进行白天发言"}

【游戏状态】
- 当前回合：第{round}回合
- 存活玩家：{alive_players}
- 已死亡玩家：{dead_players}

【你的已知信息】
- 已验身份：{checked_results}
  - Player2: 好人
  - Player5: 狼人

【昨晚死亡】
{last_night_deaths}

【历史发言摘要】
{speech_history_summary}

【推理建议】
1. 分析玩家发言寻找矛盾
2. 注意投票模式识别狼人
3. 保护自己身份避免被刀

请根据以上信息{当前任务}。
```

---

### 4. Web 后端系统 (BACKEND)

#### 4.1 技术栈 (BACKEND.STACK)

**REQ-025**: 后端技术选型
- **框架**: Spring Boot 3.x
- **Java版本**: 17+
- **构建工具**: Maven
- **实时通信**: WebSocket (Spring WebSocket + STOMP)
- **数据库**: H2 (开发环境) / MySQL (生产环境)
- **服务端口**: 8021（避免与其他Java应用冲突）

#### 4.2 REST API (BACKEND.REST_API)

**REQ-026**: 房间管理 API
```
POST   /api/rooms                    # 创建房间
GET    /api/rooms                    # 获取房间列表
GET    /api/rooms/{roomId}           # 获取房间详情
POST   /api/rooms/{roomId}/join      # 加入房间
POST   /api/rooms/{roomId}/leave     # 离开房间
POST   /api/rooms/{roomId}/start     # 开始游戏
DELETE /api/rooms/{roomId}           # 删除房间
```

**REQ-027**: 玩家管理 API
```
POST   /api/players                  # 创建玩家（用户注册/游客）
GET    /api/players/{playerId}       # 获取玩家信息
PUT    /api/players/{playerId}       # 更新玩家信息
```

**REQ-028**: 游戏配置 API
```
POST   /api/rooms/{roomId}/config    # 配置游戏（人数、AI模型）
GET    /api/models                   # 获取可用的LLM模型列表
```

#### 4.3 WebSocket 协议 (BACKEND.WEBSOCKET)

**REQ-029**: WebSocket 连接端点
```
连接: ws://localhost:8021/ws/game
订阅: /topic/room/{roomId}          # 订阅房间消息
发送: /app/game/action              # 发送游戏行动
```

**REQ-030**: WebSocket 消息格式
```json
// 服务器 → 客户端消息
{
  "type": "GAME_EVENT",
  "roomId": "room-123",
  "timestamp": 1234567890,
  "data": {
    "eventType": "PLAYER_SPEECH",
    "playerId": "player-1",
    "content": "我是预言家...",
    "metadata": {}
  }
}

// 客户端 → 服务器消息
{
  "type": "PLAYER_ACTION",
  "roomId": "room-123",
  "playerId": "player-1",
  "action": {
    "actionType": "VOTE",
    "targetId": "player-3"
  }
}
```

**REQ-031**: 消息类型定义
- GAME_STATE_UPDATE: 游戏状态更新
- PLAYER_SPEECH: 玩家发言
- VOTE_REQUEST: 投票请求
- VOTE_RESULT: 投票结果
- NIGHT_ACTION_REQUEST: 夜晚技能请求
- PHASE_CHANGE: 阶段切换（夜晚/白天）
- GAME_END: 游戏结束

#### 4.4 会话管理 (BACKEND.SESSION)

**REQ-032**: 游戏会话管理
- 每个房间对应一个游戏会话
- 会话存储：GameState、玩家连接状态、AI智能体实例
- 会话生命周期：创建 → 运行 → 结束 → 清理
- 支持会话恢复（玩家断线重连）

**REQ-033**: 并发控制
- 单个房间内的行动按序处理（避免竞态条件）
- 多个房间可并发运行
- 使用锁机制保护 GameState 修改

---

### 5. Web 前端系统 (FRONTEND)

#### 5.1 技术栈 (FRONTEND.STACK)

**REQ-034**: 前端技术选型
- **框架**: Vue 3 或 React 18 （用户选择）
- **状态管理**: Pinia (Vue) / Redux (React)
- **WebSocket**: Socket.io-client 或 SockJS
- **UI框架**: Element Plus / Ant Design （用户设计）
- **构建工具**: Vite

#### 5.2 页面结构 (FRONTEND.PAGES)

**REQ-035**: 游戏大厅页面 (Lobby)
- 显示所有可用房间列表
- 房间信息：房间ID、玩家数量、游戏状态
- 操作按钮：创建房间、加入房间、刷新列表
- 玩家信息显示：昵称、头像

**REQ-036**: 房间配置页面 (Room Config)
- 设置房间名称
- 选择游戏人数（当前固定6人）
- 配置AI玩家：
  - AI玩家数量（0-6）
  - 每个AI的模型选择（Qwen/GPT/Claude等）
  - AI性格设定（可选）
- 选择人类玩家角色（随机/指定）
- 开始游戏按钮

**REQ-037**: 游戏房间页面 (Game Room)
- **玩家列表区域**：
  - 显示所有玩家（座位号、昵称、存活状态）
  - 当前发言玩家高亮
  - 死亡玩家灰显

- **游戏信息区域**：
  - 当前阶段（夜晚N/白天N）
  - 倒计时显示
  - 自己的角色信息
  - 私密信息（如预言家验人结果）

- **消息展示区域**：
  - 实时显示游戏事件
  - 玩家发言（带玩家头像和昵称）
  - 系统消息（阶段切换、死亡公告等）
  - 自动滚动到最新消息

- **操作区域**：
  - 发言输入框（白天发言时可用）
  - 技能使用按钮（夜晚自己回合时可用）
  - 投票按钮（投票阶段可用）
  - 目标选择器（选择技能目标或投票对象）

**REQ-038**: 观战模式界面
- 可以查看所有玩家视角（切换玩家）
- 可以看到所有私密信息（真实身份、验人结果等）
- 实时更新游戏进度
- 不显示操作区域（只读模式）

#### 5.3 交互设计 (FRONTEND.INTERACTION)

**REQ-039**: 倒计时组件
- 显示当前操作剩余时间
- 倒计时归零时自动提交默认行动
- 视觉反馈：最后10秒变红色闪烁
- 音效提示（可选）

**REQ-040**: 实时消息推送
- WebSocket 连接状态指示器
- 接收到消息立即显示
- 重要消息（死亡、胜利）有特殊动画
- 消息分类显示（系统消息、发言、私聊）

**REQ-041**: 响应式设计
- 支持桌面端（1920x1080及以上）
- 适配平板端（768x1024）
- 为小程序迁移做准备（设计符合小程序规范）

---

### 6. AI 策略与推理 (AI_STRATEGY)

#### 6.1 推理引擎 (AI_STRATEGY.REASONING)

**REQ-042**: 基础推理能力
- **投票模式分析**：识别经常一起投票的玩家（可能是狼队友）
- **发言矛盾检测**：检测玩家前后发言的逻辑矛盾
- **跳神识别**：识别多人声称同一神职（如多个预言家）
- **怀疑度计算**：为每个玩家维护怀疑度评分

**REQ-043**: 记忆管理
```java
class PlayerMemory {
    List<SpeechRecord> allSpeeches;              // 所有发言记录
    Map<String, PlayerBelief> playerBeliefs;     // 对每个玩家的判断
    List<VoteRecord> voteHistory;                // 投票历史
    PrivateInfo privateInfo;                     // 私密信息（如验人结果）

    // 方法
    void addSpeech(String playerId, String content);
    void updateBelief(String playerId, double suspicion);
    List<String> getMostSuspicious(int count);
    String getMemorySummary();  // 生成记忆摘要用于Prompt
}
```

#### 6.2 角色策略 (AI_STRATEGY.ROLE_STRATEGY)

**REQ-044**: 狼人AI策略
- 夜晚协商：提议击杀目标并考虑队友建议
- 白天伪装：避免与狼队友关联，伪装好人身份
- 投票策略：优先投神职，避免集体投一人
- 发言策略：质疑跳神玩家，带节奏

**REQ-045**: 预言家AI策略
- 验人优先级：优先验发言可疑者
- 信息透露：逐步透露验人结果，避免过早暴露
- 跳神应对：面对假预言家要明确对抗
- 遗言：死前必须公开所有验人结果

**REQ-046**: 女巫AI策略
- 解药使用：评估被刀玩家价值（神职>平民）
- 毒药使用：等待确认狼人身份后使用
- 首夜自救：根据配置决定是否允许
- 隐藏身份：避免过早暴露女巫身份

**REQ-047**: 平民AI策略
- 信息收集：认真分析所有玩家发言
- 逻辑推理：帮助神职找出狼人
- 投票跟随：倾向于跟随可信的神职投票
- 诱导策略：可以假装神职吸引狼刀

---

### 7. 用户体验 (USER_EXPERIENCE)

#### 7.1 人类玩家交互 (UX.HUMAN_PLAYER)

**REQ-048**: 倒计时机制
- 人类玩家发言时间：60秒
- 人类玩家投票时间：60秒
- 人类玩家技能使用：60秒
- 倒计时归零自动处理：
  - 发言：提交空发言
  - 投票：自动弃票
  - 技能：不使用技能

**REQ-049**: 操作提示
- 当需要人类玩家操作时，明确提示操作类型
- 可选操作高亮显示（如可投票的玩家列表）
- 操作确认提醒（防止误操作）
- 快捷键支持（Enter提交，ESC取消等）

#### 7.2 观战模式 (UX.SPECTATOR)

**REQ-050**: 观战模式功能
- 进入方式：创建房间时选择"观战模式"
- 上帝视角：可以看到所有玩家真实身份
- 全知信息：可以看到所有私密消息（验人结果、狼人讨论等）
- 视角切换：可以切换到任意玩家的第一视角
- 只读模式：无法进行任何游戏操作

**REQ-051**: 观战界面特殊标识
- 玩家列表显示真实角色图标
- 消息标注可见范围（全局/私密/队伍）
- 当前视角指示器

#### 7.3 游戏节奏 (UX.GAME_PACE)

**REQ-052**: AI 响应时间
- AI发言生成：20-30秒（真实生成）
- AI投票决策：5-10秒
- AI技能使用：5-10秒
- 显示AI思考状态："AI正在思考..."

**REQ-053**: 流程平滑过渡
- 阶段切换有3秒过渡动画
- 重要事件（死亡、胜利）有特殊动画
- 背景音乐和音效（可选，可关闭）

---

### 8. 系统非功能性需求 (NON_FUNCTIONAL)

#### 8.1 性能要求 (NFR.PERFORMANCE)

**REQ-054**: 响应时间
- WebSocket 消息推送延迟 < 1秒
- AI发言生成时间 < 30秒
- REST API 响应时间 < 500ms
- 页面首次加载时间 < 3秒

**REQ-055**: 并发支持
- 支持至少10个房间并发运行
- 单个房间支持6个玩家 + 若干观战者
- WebSocket 连接数限制：100个并发连接

#### 8.2 可靠性 (NFR.RELIABILITY)

**REQ-056**: 错误处理
- LLM API 调用失败时重试3次
- 重试失败后使用默认行动（如随机投票）
- 所有异常必须被捕获并记录
- 用户友好的错误提示

**REQ-057**: 会话恢复
- 人类玩家断线后60秒内可重连
- 重连后恢复到当前游戏状态
- AI玩家异常时自动替换为简单策略AI

#### 8.3 可维护性 (NFR.MAINTAINABILITY)

**REQ-058**: 代码规范
- 遵循Java编码规范（Google Java Style Guide）
- 关键类和方法必须有Javadoc注释
- 单元测试覆盖率 > 60%

**REQ-059**: 日志记录
- 所有游戏事件记录到日志
- LLM API 调用记录（请求、响应、耗时）
- 错误和异常堆栈记录
- 使用分级日志（INFO / WARN / ERROR）

#### 8.4 安全性 (NFR.SECURITY)

**REQ-060**: API 安全
- LLM API Key 使用环境变量存储
- 前端不暴露 API Key
- WebSocket 连接验证（简单 token 认证）

**REQ-061**: 输入验证
- 所有用户输入必须验证和过滤
- 防止SQL注入（使用ORM）
- 防止XSS攻击（前端转义）

---

## MODIFIED Requirements

（无，这是初始项目）

---

## REMOVED Requirements

（无，这是初始项目）

---

## Dependencies & Integration Points

### 外部依赖
- AgentScope (Java) - 多智能体框架
- LLM API服务（通过中转站）
- Spring Boot框架
- WebSocket库

### 内部依赖
- GAME_CORE → AGENT_SYSTEM（游戏引擎被智能体使用）
- AGENT_SYSTEM → LLM_INTEGRATION（智能体依赖LLM模型）
- BACKEND → GAME_CORE + AGENT_SYSTEM（后端整合两者）
- FRONTEND → BACKEND（前端调用后端API）

---

## Testing Requirements

### 单元测试
- 游戏规则引擎测试（各种边界情况）
- 角色技能测试
- 胜利条件判定测试
- Prompt 模板生成测试

### 集成测试
- GameMaster + AIPlayer 完整流程测试
- WebSocket 消息推送测试
- LLM 适配器测试（使用 Mock）

### 端到端测试
- 完整6人局游戏流程测试（至少1局狼胜、1局人胜）
- 人类玩家参与测试
- 观战模式测试

---

## Migration & Rollback

不适用（初始版本）

---

## Documentation Requirements

**REQ-062**: 必须提供以下文档
- README.md：项目介绍、快速开始
- API文档：Swagger/OpenAPI规范
- 部署文档：环境配置、启动步骤
- 配置文档：LLM模型配置说明

---

## Future Considerations

以下功能不在本次变更范围内，但需要在架构设计时预留扩展性：

1. **12人及多人局**：角色系统设计要易于扩展
2. **更多角色**：猎人、守卫、白狼王、守墓人等
3. **小程序版本**：API设计要兼容小程序调用
4. **游戏录像**：GameEvent 设计要支持完整回放
5. **AI训练**：考虑收集游戏数据用于AI策略优化

---

**Last Updated**: 2025-12-11
**Status**: Ready for Implementation
