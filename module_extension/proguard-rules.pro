# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================================================
# TrustExtensions 属性委托 ProGuard 规则
# ============================================================================
#
# 如果使用属性委托时没有指定自定义 key（如 intIntent()、intArgument() 等），
# 属性名会被用作 Bundle/Intent 的 key。R8/ProGuard 混淆后属性名会变化，
# 导致参数读取失败。
#
# 解决方案（二选一）：
#
# 方案 1（推荐）：使用自定义 key
#   private val userId by intIntent(key = "user_id")
#   private var userName: String by stringArgument(key = "user_name")
#
# 方案 2：使用 @Keep 注解 + 下面的 ProGuard 规则
#   @Keep private val userId by intIntent()
#
# ============================================================================

# 保留使用 @Keep 注解的属性名（不混淆属性名，但允许其他优化）
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
}

# 如果需要保留特定 Activity/Fragment 中所有使用委托的属性，可以使用：
# -keepclassmembers class com.example.YourActivity {
#     private *** *;
# }
#
# 或者保留所有 Activity/Fragment 的属性名（不推荐，影响混淆效果）：
# -keepclassmembers class * extends android.app.Activity {
#     private *** *;
# }
# -keepclassmembers class * extends androidx.fragment.app.Fragment {
#     private *** *;
# }