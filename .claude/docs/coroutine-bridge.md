# 协程桥接：suspendCancellableCoroutine & callbackFlow

将回调式 API（Listener、SDK 回调）接入协程世界的两种标准手段。

---

## 选型原则

| 场景 | 工具 |
|------|------|
| 一次性结果（请求/响应） | `suspendCancellableCoroutine` |
| 持续多次事件（传感器、位置、Socket） | `callbackFlow` |

---

## suspendCancellableCoroutine

### 核心机制

挂起当前协程，把 `Continuation`（协程的"恢复句柄"）暴露给外部，由回调手动 `resume`。
协程取消时，`invokeOnCancellation` 会被调用，用于清理资源。

```
协程挂起 ──────────────► 回调触发
         ◄── cont.resume ── onSuccess / onError
```

### 基本模板

```kotlin
suspend fun <T> doSomethingAsync(): T = suspendCancellableCoroutine { cont ->
    val callback = object : SomeCallback {
        override fun onSuccess(result: T) {
            cont.resume(result)                    // 恢复，返回值
        }
        override fun onError(e: Exception) {
            cont.resumeWithException(e)            // 恢复，抛出异常
        }
    }

    val task = someApi.start(callback)

    cont.invokeOnCancellation {                    // 协程取消时清理
        task.cancel()
    }
}
```

### 实战：Google 登录

```kotlin
suspend fun getCredential(context: Context, request: GetCredentialRequest) =
    suspendCancellableCoroutine<GetCredentialResponse> { cont ->
        CredentialManager.create(context)
            .getCredentialAsync(
                request = request,
                context = context,
                cancellationSignal = CancellationSignal().also { signal ->
                    cont.invokeOnCancellation { signal.cancel() }
                },
                executor = Executors.newSingleThreadExecutor(),
                callback = object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                    override fun onResult(result: GetCredentialResponse) = cont.resume(result)
                    override fun onError(e: GetCredentialException) = cont.resumeWithException(e)
                },
            )
    }
```

### 实战：权限请求（单次）

```kotlin
// 在 Activity 中
private var permissionCont: Continuation<Boolean>? = null

suspend fun requestPermission(permission: String): Boolean =
    suspendCancellableCoroutine { cont ->
        permissionCont = cont
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE)
        cont.invokeOnCancellation { permissionCont = null }
    }

// onRequestPermissionsResult 中恢复
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    if (requestCode == REQUEST_CODE) {
        permissionCont?.resume(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
        permissionCont = null
    }
}
```

### cont.resume 的三种形式

```kotlin
cont.resume(value)                 // 成功，返回 value
cont.resumeWithException(e)        // 失败，抛出异常（调用方 try/catch 捕获）
cont.resume(Result.failure(e))     // 失败，以 Result 包装（不抛异常）
```

> **注意**：`resume` 只能调用一次，重复调用会抛 `IllegalStateException`。

---

## callbackFlow

### 核心机制

创建一个冷 Flow，内部持有一个 Channel。回调触发时向 Channel `send`，Flow 的收集者消费数据。
Flow 被取消或收集者离开时，`awaitClose` 中的清理逻辑执行。

```
注册回调 ──► 事件触发 ──► trySend ──► Channel ──► collect 收集
                                                ↑
                           Flow 取消 ──► awaitClose { 注销回调 }
```

### 基本模板

```kotlin
fun observeSomething(): Flow<T> = callbackFlow {
    val listener = object : SomeListener {
        override fun onEvent(data: T) {
            trySend(data)           // 非挂起发送，失败静默丢弃（Channel 已关闭时）
        }
        override fun onError(e: Exception) {
            close(e)                // 以异常关闭 Flow
        }
    }

    someSource.register(listener)

    awaitClose {                    // Flow 取消 / 收集完成时调用
        someSource.unregister(listener)
    }
}
```

### 实战：位置更新

```kotlin
fun Context.locationUpdates(intervalMs: Long): Flow<Location> = callbackFlow {
    val client = LocationServices.getFusedLocationProviderClient(this@locationUpdates)
    val request = LocationRequest.Builder(intervalMs).build()

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { trySend(it) }
        }
    }

    client.requestLocationUpdates(request, callback, Looper.getMainLooper())

    awaitClose { client.removeLocationUpdates(callback) }
}

// 使用
viewModelScope.launch {
    context.locationUpdates(5_000L)
        .collect { location -> updateUI(location) }
}
```

### 实战：传感器数据

```kotlin
fun SensorManager.accelerometerFlow(): Flow<FloatArray> = callbackFlow {
    val sensor = getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            trySend(event.values.clone())
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

    awaitClose { unregisterListener(listener) }
}
```

### 实战：Room 以外的数据库监听

```kotlin
fun observeTable(): Flow<List<Item>> = callbackFlow {
    val observer = object : DataObserver() {
        override fun onChange() {
            // 数据变更时查询并发送
            launch { trySend(db.query()) }
        }
    }
    db.registerObserver(observer)
    trySend(db.query())             // 立即发送当前值

    awaitClose { db.unregisterObserver(observer) }
}
```

### trySend vs send

| | `trySend` | `send` |
|---|---|---|
| 是否挂起 | 否 | 是（Channel 满时挂起） |
| 回调中可用 | ✓ | ✗（普通回调无法调用 suspend） |
| 失败处理 | 静默丢弃（返回 `ChannelResult`） | 抛异常 |

> 回调中**必须**用 `trySend`；如果在协程内部可用 `send`。

### close vs cancel

```kotlin
close()         // 正常关闭，收集者收到所有已发送元素后结束
close(e)        // 以异常关闭，收集者抛出异常
cancel()        // 立即取消，未消费元素丢弃
```

---

## 两者对比

```
一次性：
  回调 ──► cont.resume ──► 协程恢复 ──► 返回结果
  
持续：
  回调1 ──► trySend ──┐
  回调2 ──► trySend ──┤──► Channel ──► collect { ... }
  回调3 ──► trySend ──┘
```

### 陷阱对照

| | suspendCancellableCoroutine | callbackFlow |
|---|---|---|
| 忘记 `invokeOnCancellation` | 资源泄漏（回调悬空） | `awaitClose` 必填，编译期强制 |
| 多次 `resume` | `IllegalStateException` | `trySend` 幂等，安全 |
| 在非协程上下文调用 | 不可用 | `trySend` 可在任意线程调用 |
| 背压 | 无（单值） | Channel 默认 `BUFFERED`（64容量），溢出丢弃 |

---

## 与 CompletableDeferred 区别

三者都能桥接回调，定位不同：

| | `suspendCancellableCoroutine` | `CompletableDeferred` | `callbackFlow` |
|---|---|---|---|
| 结果数量 | 单次 | 单次 | 多次 |
| 取消传播 | 自动（invokeOnCancellation） | 手动 | 自动（awaitClose） |
| 跨协程通信 | 不适合 | 适合（可传递引用） | 不适合 |
| 推荐度 | ★★★（底层首选） | ★★（特定场景） | ★★★（多事件首选） |

> `CompletableDeferred` 适合"协程 A 等待协程 B 的结果"这类**协程间通信**，不适合直接桥接 SDK 回调。
