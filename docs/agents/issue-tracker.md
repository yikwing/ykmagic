# Issue Tracker: Local Markdown

Issues 以本地 markdown 文件形式存储在此仓库的 `.scratch/<feature>/` 目录下。

## 工作流

### 创建 Issue

```bash
mkdir -p .scratch/<feature-name>
cat > .scratch/<feature-name>/issue.md <<EOF
# Issue: <标题>

## 描述

<问题或功能请求的详细描述>

## 验收标准

- [ ] 标准 1
- [ ] 标准 2

## 状态

- **Created**: $(date +%Y-%m-%d)
- **Label**: needs-triage
EOF
```

### 更新 Issue

直接编辑 `.scratch/<feature-name>/issue.md` 文件。

### 添加标签

在 Issue 文件的 `## 状态` 部分更新 `Label` 字段：

```markdown
## 状态

- **Label**: ready-for-agent
```

### 关闭 Issue

选项 1：删除整个 `.scratch/<feature-name>/` 目录  
选项 2：在 Issue 文件中添加 `- **Closed**: $(date +%Y-%m-%d)`

## 约定

- 一个功能一个目录
- 主 Issue 文件命名为 `issue.md`
- 相关的笔记、草图或子任务可以添加为同一目录下的额外文件
- `.scratch/` 目录已在 `.gitignore` 中（或应该被忽略）——Issues 不会提交到版本控制
