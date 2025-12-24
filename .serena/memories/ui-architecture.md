# UI 架构 (Jetpack Compose)

## 目录结构
```
app/src/main/java/com/yikwing/ykquickdev/ui/
├── screen/          # 页面
│   ├── MainScreen.kt
│   ├── DiyInputScreen.kt
│   ├── PackageInfoScreen.kt
│   └── OtherPageScreen.kt
├── widget/          # 组件
│   ├── UIRoot.kt
│   ├── ShapeLayout.kt
│   ├── GradientPage.kt
│   └── BSheet.kt
├── theme/           # 主题
│   ├── Color.kt
│   └── Type.kt
├── utils/           # 工具
│   ├── DesignScale.kt
│   └── adjustBrightness.kt
└── ComposeActivity.kt
```

## 主要组件
- `UIRoot` - 根布局组件
- `ShapeLayout` - 形状布局
- `GradientPage` - 渐变页面
- `DesignScale` - 375dp 设计稿适配
