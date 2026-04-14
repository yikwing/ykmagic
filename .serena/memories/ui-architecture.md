# UI 架构 (Jetpack Compose + Navigation 3)

## 目录结构
```
app/src/main/java/com/yikwing/ykquickdev/ui/
├── screen/          # 页面 (Nav3 三层: Route/Entry/Screen)
│   ├── MainScreen.kt (HorizontalPager + BottomBar)
│   ├── TextDebounceScreen.kt
│   ├── PackageInfoScreen.kt
│   ├── OtherPageScreen.kt
│   ├── DiyInputScreen.kt
│   ├── AuthLoginScreen.kt
│   └── AuthRegisterScreen.kt
├── widget/          # 页面级非通用组件
│   ├── UIRoot.kt / ShapeLayout.kt / GradientPage.kt
│   ├── BSheet.kt / ConstraintPage.kt
│   └── (已无 SystemBarsStyle.kt, 移到 module_compose)
├── theme/           # Material3 主题
│   ├── Color.kt
│   └── Type.kt
├── utils/           # UI 工具
│   ├── DesignScale.kt (sdp 适配)
│   ├── adjustBrightness.kt
│   └── NavBackStackExtensions.kt
├── BottomNavBar.kt  # 底部导航栏
└── ComposeActivity.kt
```

## app 内 components/
- `Center.kt` — 居中布局 (app 层封装)
- `VerificationCodeTextField.kt` — 验证码输入
- `LocalNavigation.kt` — CompositionLocal 导航

## 导航
- Navigation 3 (NavKey + NavBackStack + NavDisplay)
- 路由定义在 `app/.../app/AppNavGraph.kt`
- 三层分离: Route (@Serializable NavKey) → Entry (注册路由) → Screen (纯 UI)