# AI Agent Instructions for Werewolf Platform

## Project Overview

This is a multi-agent werewolf (狼人杀) game platform where human players can play with AI agents powered by different LLM models.

## Development Workflow

This project uses **OpenSpec** for specification-driven development.

### OpenSpec Process

1. **Draft Proposals** - Create change folders with proposal.md, tasks.md, and spec deltas
2. **Review & Align** - Iterate on specifications with the team
3. **Implement Tasks** - Write code based on agreed specifications
4. **Archive Updates** - Merge completed changes back to main specs

### Key Directories

- `openspec/specs/` - Current source of truth specifications
- `openspec/changes/` - Active change proposals
- `openspec/archived/` - Completed and archived changes

## AI Assistant Guidelines

### Before Writing Code

1. **Always check existing specs** in `openspec/specs/`
2. **Create a change proposal** for any new feature or modification
3. **Get human approval** on specifications before implementation
4. **Use structured spec deltas**:
   - `## ADDED Requirements` - New functionality
   - `## MODIFIED Requirements` - Changed behavior
   - `## REMOVED Requirements` - Deprecated features

### When Creating Changes

```bash
# Validate your change
openspec validate <change-name>

# View all active changes
openspec list

# Show change details
openspec show <change-name>
```

### Implementation Phase

- Only implement tasks from approved proposals
- Reference spec sections in code comments
- Update tasks.md as you complete items
- Run tests and validation before marking tasks complete

## Project-Specific Context

### Technology Stack

- **Framework**: AgentScope (Java) for multi-agent orchestration
- **Language**: Java 17+
- **Build Tool**: Maven
- **LLM Integration**: Multiple providers (Qwen, GPT, Claude, Gemini)
- **Frontend**: TBD (Web UI or CLI)

### Key Components

1. **GameMaster Agent** - Controls game flow and rules
2. **AI Player Agents** - LLM-powered players with reasoning capabilities
3. **Human Player Interface** - Interface for human participation
4. **Role System** - Werewolf, Seer, Witch, Hunter, Guard, Villager
5. **Communication Hub** - Message broadcasting system

### Game Rules Reference

Standard 12-player werewolf game:
- 4 Werewolves
- 4 Villagers
- 1 Seer (查验身份)
- 1 Witch (解药+毒药)
- 1 Hunter (死亡开枪)
- 1 Guard/Idiot (守护/白痴)

## Commands for This Project

### Starting New Features

When user requests a new feature:
1. Create change proposal: `mkdir -p openspec/changes/feature-name`
2. Write `proposal.md` describing the feature
3. Create `spec-delta.md` with structured requirements
4. List implementation steps in `tasks.md`
5. Await approval before coding

### Reviewing Changes

Before implementing:
- Run `openspec validate <change-name>`
- Ensure all specs are clear and unambiguous
- Check for conflicts with existing specs
- Get explicit approval from user

## Communication Style

- Use Chinese (中文) when communicating with the user
- Be concise and structured in responses
- Always reference spec sections when discussing features
- Ask clarifying questions before making assumptions

## Current Focus

We are in the **specification phase** for the initial werewolf platform. Focus on:
1. Defining clear game flow specifications
2. Designing multi-agent interaction patterns
3. Planning human-AI hybrid gameplay mechanics
4. Establishing LLM integration architecture

---

*Last Updated: 2025-12-11*
