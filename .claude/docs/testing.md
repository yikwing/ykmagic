# 测试指南

## Hamcrest 匹配器

项目使用 Hamcrest 进行断言，提供更可读的测试代码。

### 一般匹配符

```kotlin
// allOf：所有条件必须都成立，测试才通过
assertThat(s, allOf(greaterThan(1), lessThan(3)))

// anyOf：只要有一个条件成立，测试就通过
assertThat(s, anyOf(greaterThan(1), lessThan(1)))

// anything：无论什么条件，测试都通过
assertThat(s, anything())

// is：变量的值等于指定值时，测试通过
assertThat(s, `is`(2))

// not：和 is 相反，变量的值不等于指定值时，测试通过
assertThat(s, not(1))
```

### 数值匹配符

```kotlin
// closeTo：浮点型变量的值在 3.0±0.5 范围内，测试通过
assertThat(d, closeTo(3.0, 0.5))

// greaterThan：变量的值大于指定值时，测试通过
assertThat(d, greaterThan(3.0))

// lessThan：变量的值小于指定值时，测试通过
assertThat(d, lessThan(3.5))

// greaterThanOrEqualTo：变量的值大于等于指定值时，测试通过
assertThat(d, greaterThanOrEqualTo(3.3))

// lessThanOrEqualTo：变量的值小于等于指定值时，测试通过
assertThat(d, lessThanOrEqualTo(3.4))
```

### 字符串匹配符

```kotlin
// containsString：字符串变量中包含指定字符串时，测试通过
assertThat(n, containsString("ci"))

// startsWith：字符串变量以指定字符串开头时，测试通过
assertThat(n, startsWith("Ma"))

// endsWith：字符串变量以指定字符串结尾时，测试通过
assertThat(n, endsWith("i"))

// equalTo：字符串变量等于指定字符串时，测试通过
assertThat(n, equalTo("Magci"))

// equalToIgnoringCase：忽略大小写比较
assertThat(n, equalToIgnoringCase("magci"))

// equalToIgnoringWhiteSpace：忽略头尾空格比较
assertThat(n, equalToIgnoringWhiteSpace(" Magci   "))
```

### 集合匹配符

```kotlin
// hasItem：Iterable 变量中含有指定元素时，测试通过
assertThat(list, hasItem("Magci"))

// hasEntry：Map 变量中含有指定键值对时，测试通过
assertThat(map, hasEntry("key", "value"))

// hasKey：Map 变量中含有指定键时，测试通过
assertThat(map, hasKey("key"))

// hasValue：Map 变量中含有指定值时，测试通过
assertThat(map, hasValue("value"))
```

## 运行测试

```bash
# 运行所有单元测试
./gradlew test

# 运行指定模块测试
./gradlew :module_config:test

# 运行单个测试类
./gradlew test --tests "com.yikwing.config.YkConfigManagerTest"

# 运行 Instrumented 测试
./gradlew connectedDebugAndroidTest
```