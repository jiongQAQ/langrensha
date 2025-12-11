# 🐺 狼人杀多智能体游戏平台

基于AgentScope和多种大语言模型的智能狼人杀游戏平台，支持人类玩家与AI智能体混合对战。

## 项目结构

```
langrensha/
├── project/
│   └── backend/          # 后端服务（Spring Boot）
│       ├── src/          # 源代码
│       ├── openspec/     # OpenSpec规范文档
│       ├── pom.xml       # Maven配置
│       ├── README.md     # 后端详细文档
│       └── .gitignore    # 后端忽略文件
└── README.md             # 项目总览（本文件）
```

## 快速开始

### 后端服务

进入后端目录：

```bash
cd project/backend
```

查看 [后端README](project/backend/README.md) 了解详细的启动和配置说明。

## 技术栈

- **后端**: Java 17 + Spring Boot 3.2.0
- **数据库**: H2 (开发) / MySQL (生产)
- **实时通信**: WebSocket (STOMP)
- **AI模型**: Qwen, GPT, Claude, Gemini (通过API中转站)

## 开发进度

当前版本: **v0.1.0-SNAPSHOT**

**Phase 1: 核心游戏引擎** (进行中 - 55.6%)
- ✅ TASK-001: 创建Maven项目结构
- ✅ TASK-002: 配置Spring Boot开发环境
- ✅ TASK-003: 实现角色系统
- ✅ TASK-004: 实现游戏状态模型
- ✅ TASK-005: 实现夜晚流程引擎
- ⏳ TASK-006: 实现白天流程引擎
- ⏳ TASK-007: 实现胜利条件判定
- ⏳ TASK-008: 实现游戏主控制器
- ⏳ TASK-009: 编写核心引擎测试

## 文档

- [后端服务文档](project/backend/README.md)
- [项目提案](project/backend/openspec/changes/001-initialize-werewolf-platform/proposal.md)
- [详细规范](project/backend/openspec/changes/001-initialize-werewolf-platform/spec-delta.md)
- [任务清单](project/backend/openspec/changes/001-initialize-werewolf-platform/tasks.md)
- [配置指南](project/backend/openspec/changes/001-initialize-werewolf-platform/config-guide.md)

## 游戏特性

- 🎮 **6人局狼人杀**: 2狼2民1预1女标准配置
- 🤖 **多模型支持**: Qwen、GPT、Claude、Gemini等
- 🌐 **Web实时对战**: WebSocket实时通信
- 👁️ **观战模式**: 上帝视角观看AI对局
- 🧠 **智能推理**: AI具备逻辑推理和策略能力

## License

MIT License

## 作者

- User (项目发起人)
- Claude Sonnet 4.5 (AI开发助手)

---

**最后更新**: 2025-12-11
