
# Implementation Plan: 羽毛球约球与消费记录系统

**Branch**: `001-` | **Date**: 2025-09-22 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-/spec.md`

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
   → If not found: ERROR "No feature spec at {path}"
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
   → Detect Project Type from context (web=frontend+backend, mobile=app+api)
   → Set Structure Decision based on project type
3. Fill the Constitution Check section based on the content of the constitution document.
4. Evaluate Constitution Check section below
   → If violations exist: Document in Complexity Tracking
   → If no justification possible: ERROR "Simplify approach first"
   → Update Progress Tracking: Initial Constitution Check
5. Execute Phase 0 → research.md
   → If NEEDS CLARIFICATION remain: ERROR "Resolve unknowns"
6. Execute Phase 1 → contracts, data-model.md, quickstart.md, agent-specific template file (e.g., `CLAUDE.md` for Claude Code, `.github/copilot-instructions.md` for GitHub Copilot, `GEMINI.md` for Gemini CLI, `QWEN.md` for Qwen Code or `AGENTS.md` for opencode).
7. Re-evaluate Constitution Check section
   → If new violations: Refactor design, return to Phase 1
   → Update Progress Tracking: Post-Design Constitution Check
8. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
9. STOP - Ready for /tasks command
```

**IMPORTANT**: The /plan command STOPS at step 7. Phases 2-4 are executed by other commands:
- Phase 2: /tasks command creates tasks.md
- Phase 3-4: Implementation execution (manual or via tools)

## Summary
羽毛球约球与消费记录系统 - 一个简单实用的约球社交应用，支持快速发起约球活动、邀请好友参与，并记录活动费用进行自动分摊。系统采用Redis作为唯一数据存储，前端基于开源脚手架开发，确保低复杂度和快速迭代。

## Technical Context
**Language/Version**: Java 17 + Spring Boot 3.x (后端), Vue 3.x + TypeScript (前端)  
**Primary Dependencies**: Spring Boot Web, Spring Data Redis, Vue3 + Vite + Element Plus  
**Storage**: Redis 7.0 (唯一数据存储，简化架构)  
**Testing**: JUnit 5 + Testcontainers (后端), Vitest + Vue Test Utils (前端)  
**Target Platform**: Linux server + 移动端H5 + 小程序
**Project Type**: web - 前后端分离架构  
**Performance Goals**: API响应<200ms P95, 页面加载<2秒, 支持100并发用户  
**Constraints**: 无支付功能, 无消息队列, 单Redis存储, 基于开源脚手架开发  
**Scale/Scope**: 初期500活跃用户, 约10个API接口, 5个主要页面

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### 原则符合性评估
✅ **用户体验至上**: 采用Vue3 + Element Plus确保界面美观，Redis快速响应保证<2秒交互体验，移动端优先设计
✅ **社交化增强粘性**: 核心功能就是约球社交，支持好友邀请、费用分摊等社交场景
✅ **数据驱动优化**: Redis存储用户行为数据，支持个性化推荐和统计分析
✅ **快速迭代部署**: 前后端分离架构，支持独立部署和灰度发布
❌ **可扩展架构设计**: 与宪章要求存在偏差 - 选择单Redis存储而非微服务+MySQL

### 技术栈偏差分析
| 宪章要求 | 实际选择 | 偏差原因 |
|---------|----------|----------|
| MySQL + Redis | 仅Redis | 数据简单，避免过度设计 |
| RabbitMQ | 无MQ | 实时性要求不高，简化架构 |
| 微服务架构 | 单体应用 | 初期规模小，降低复杂度 |

### 偏差正当性
选择简化技术栈符合用户要求的"复杂度不能太高"，且：
- Redis足够支持初期500用户规模
- 费用记录等数据结构简单，无需复杂关系型数据库
- 约球应用实时性要求不高，无需消息队列
- 可在需要时平滑升级到完整架构

## Project Structure

### Documentation (this feature)
```
specs/[###-feature]/
├── plan.md              # This file (/plan command output)
├── research.md          # Phase 0 output (/plan command)
├── data-model.md        # Phase 1 output (/plan command)
├── quickstart.md        # Phase 1 output (/plan command)
├── contracts/           # Phase 1 output (/plan command)
└── tasks.md             # Phase 2 output (/tasks command - NOT created by /plan)
```

### Source Code (repository root)
```
# Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure]
```

**Structure Decision**: Option 2 - Web application (frontend + backend)

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context** above:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:
   ```
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all NEEDS CLARIFICATION resolved

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

3. **Generate contract tests** from contracts:
   - One test file per endpoint
   - Assert request/response schemas
   - Tests must fail (no implementation yet)

4. **Extract test scenarios** from user stories:
   - Each story → integration test scenario
   - Quickstart test = story validation steps

5. **Update agent file incrementally** (O(1) operation):
   - Run `.specify/scripts/bash/update-agent-context.sh claude`
     **IMPORTANT**: Execute it exactly as specified above. Do not add or remove any arguments.
   - If exists: Add only NEW tech from current plan
   - Preserve manual additions between markers
   - Update recent changes (keep last 3)
   - Keep under 150 lines for token efficiency
   - Output to repository root

**Output**: data-model.md, /contracts/*, failing tests, quickstart.md, agent-specific file

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
- Load `.specify/templates/tasks-template.md` as base
- Generate tasks from Phase 1 design docs (contracts, data model, quickstart)
- Each contract → contract test task [P]
- Each entity → model creation task [P] 
- Each user story → integration test task
- Implementation tasks to make tests pass

**Ordering Strategy**:
- TDD order: Tests before implementation 
- Dependency order: Models before services before UI
- Mark [P] for parallel execution (independent files)

**Estimated Output**: 25-30 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)  
**Phase 4**: Implementation (execute tasks.md following constitutional principles)  
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| 单Redis存储代替MySQL+Redis | 简化架构，降低运维复杂度，满足用户"复杂度不能太高"要求 | MySQL关系型存储对约球费用等简单数据是过度设计 |
| 无消息队列 | 约球通知可通过轮询或WebSocket实现，避免引入额外中间件 | 对于小规模应用，MQ增加了不必要的架构复杂度 |
| 单体应用代替微服务 | 降低部署和开发复杂度，适合初期快速迭代 | 微服务架构对10个接口的小应用是过度工程化 |


## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command) - ✅ 2025-09-22
- [x] Phase 1: Design complete (/plan command) - ✅ 2025-09-22
- [x] Phase 2: Task planning complete (/plan command - describe approach only) - ✅ 2025-09-22
- [ ] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS - ✅ 架构偏差已识别并正当化
- [x] Post-Design Constitution Check: PASS - ✅ 设计符合简化要求
- [x] All NEEDS CLARIFICATION resolved - ✅ 无待澄清项
- [x] Complexity deviations documented - ✅ 已记录在复杂度跟踪表

---
*Based on Constitution v1.0.0 - See `/memory/constitution.md`*
