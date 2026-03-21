# UI 架构 (Jetpack Compose)

## 目录结构
```
app/src/main/java/com/yikwing/ykquickdev/ui/
├── screen/          # 页面
│   ├── MainScreen.kt
│   ├── DiyInputScreen.kt
│   ├── PackageInfoScreen.kt
│   ├── OtherPageScreen.kt
│   └── TextDebounceScreen.kt
├── widget/          # 组件
│   ├── UIRoot.kt
│   ├── ShapeLayout.kt
│   ├── GradientPage.kt
│   ├── BSheet.kt
│   ├── ConstraintPage.kt
│   └── SystemBarsStyle.kt
├── theme/           # 主题
│   ├── Color.kt
│   └── Type.kt
├── utils/           # 工具
│   ├── DesignScale.kt
│   ├── adjustBrightness.kt
│   ├── DebounceClick.kt
│   ├── NavBackStackExtensions.kt
│   └── OnEnterEffect.kt
└── ComposeActivity.kt
```

## 通用 Compose 组件 (app/.../components/)
- `ImageWidget.kt` - 图片组件
- `LoadingWidget.kt` - 加载组件
- `NetWorkError.kt` - 网络错误组件
- `VerificationCodeTextField.kt` - 验证码输入
- `Center.kt` - 居中布局
- `NoIndication.kt` - 无水波纹

## 主要组件
- `UIRoot` - 根布局组件
- `ShapeLayout` - 形状布局
- `GradientPage` - 渐变页面
- `DesignScale` - 375dp 设计稿适配