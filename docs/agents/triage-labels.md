# Triage Labels

本仓库使用以下标签词汇进行 Issue 分类：

| 角色 | 标签字符串 | 含义 |
|------|-----------|------|
| 需要分类 | `needs-triage` | 维护者需要评估此 Issue |
| 需要信息 | `needs-info` | 等待报告者提供更多信息 |
| Agent 就绪 | `ready-for-agent` | 完全指定，AFK 就绪（Agent 可以在没有人类上下文的情况下处理） |
| 人类就绪 | `ready-for-human` | 需要人类实现 |
| 不修复 | `wontfix` | 不会被处理 |

## 使用说明

在本地 markdown Issue 追踪器中，标签存储在 Issue 文件的 `## 状态` 部分：

```markdown
## 状态

- **Label**: ready-for-agent
```

Triage 技能会自动更新此字段。
