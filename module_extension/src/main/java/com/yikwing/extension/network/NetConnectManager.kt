package com.yikwing.extension.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import java.io.Closeable

/**
 * 网络连接类型
 */
enum class NetworkType {
    /** WiFi 网络 */
    WIFI,

    /** 移动数据网络 */
    CELLULAR,

    /** 以太网 */
    ETHERNET,

    /** VPN 网络 */
    VPN,

    /** 无网络连接 */
    NONE,
}

/**
 * 网络状态数据类
 *
 * @property isConnected 是否已连接网络
 * @property type 网络类型
 */
data class NetworkState(
    val isConnected: Boolean = false,
    val type: NetworkType = NetworkType.NONE,
)

/**
 * 网络连接管理器
 *
 * 使用 Flow 提供响应式的网络状态监听，支持 Compose 和协程。
 *
 * ## 特性
 * - 使用 StateFlow 暴露网络状态，支持 Compose collectAsState
 * - 提供一次性查询 API
 * - 支持 VPN 检测（独立于底层网络类型）
 * - 实现 Closeable 接口，支持显式资源清理
 * - 线程安全（Koin 单例）
 * - 生命周期与 Application 相同
 *
 * ## 使用示例
 *
 * ### Koin 注入
 * ```kotlin
 * // 在 ViewModel 中
 * @KoinViewModel
 * class MyViewModel(
 *     private val netConnectManager: NetConnectManager
 * ) : ViewModel()
 *
 * // 在其他类中
 * class MyRepository(
 *     private val netConnectManager: NetConnectManager
 * )
 * ```
 *
 * ### Compose 中使用
 * ```kotlin
 * val networkState by netConnectManager.networkState.collectAsState()
 *
 * if (networkState.isConnected) {
 *     Text("已连接: ${networkState.type}")
 * } else {
 *     Text("无网络连接")
 * }
 * ```
 *
 * ### 协程中使用
 * ```kotlin
 * // 监听网络状态变化
 * lifecycleScope.launch {
 *     repeatOnLifecycle(Lifecycle.State.STARTED) {
 *         netConnectManager.networkState.collect { state ->
 *             Log.d("Network", "Connected: ${state.isConnected}, Type: ${state.type}")
 *         }
 *     }
 * }
 *
 * // 仅监听连接状态
 * netConnectManager.isConnected.collect { connected ->
 *     if (connected) loadData()
 * }
 *
 * // 仅监听网络类型
 * netConnectManager.networkType.collect { type ->
 *     when (type) {
 *         NetworkType.WIFI -> enableHighQuality()
 *         NetworkType.CELLULAR -> enableDataSaver()
 *         NetworkType.VPN -> handleVpnConnection()
 *         else -> showOffline()
 *     }
 * }
 * ```
 *
 * ### 一次性查询
 * ```kotlin
 * if (netConnectManager.isCurrentlyConnected) {
 *     // 执行网络操作
 * }
 *
 * when (netConnectManager.currentNetworkType) {
 *     NetworkType.WIFI -> // WiFi 网络
 *     NetworkType.CELLULAR -> // 移动网络
 *     NetworkType.VPN -> // 纯 VPN 网络
 *     else -> // 无网络
 * }
 *
 * // 检测 VPN 是否激活（即使底层是 WiFi 或移动网络）
 * if (netConnectManager.isVpnActive) {
 *     // VPN 已激活，可能同时有 WiFi/Cellular 底层连接
 * }
 * ```
 */
@Single
class NetConnectManager(
    context: Context,
) : Closeable {
    private val connectivityManager: ConnectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val _networkState = MutableStateFlow(NetworkState())

    /** 网络状态 StateFlow，包含连接状态和网络类型 */
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    /** 是否已连接网络的 Flow */
    val isConnected: Flow<Boolean> = networkState.map { it.isConnected }.distinctUntilChanged()

    /** 网络类型的 Flow */
    val networkType: Flow<NetworkType> = networkState.map { it.type }.distinctUntilChanged()

    /** 一次性查询：当前是否已连接网络 */
    val isCurrentlyConnected: Boolean get() = networkState.value.isConnected

    /** 一次性查询：当前网络类型 */
    val currentNetworkType: NetworkType get() = networkState.value.type

    /** 一次性查询：VPN 是否激活（无论底层网络类型） */
    val isVpnActive: Boolean
        get() =
            connectivityManager.activeNetwork?.let {
                connectivityManager
                    .getNetworkCapabilities(it)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            } ?: false

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // 网络可用时，等待 onCapabilitiesChanged 获取详细信息
            }

            override fun onLost(network: Network) {
                _networkState.value = queryCurrentNetworkState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) {
                _networkState.value = parseNetworkState(capabilities)
            }
        }

    init {
        // registerDefaultNetworkCallback 会立即触发一次 onCapabilitiesChanged 回调当前状态
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    // ==================== Private ====================

    private fun queryCurrentNetworkState(): NetworkState {
        val network = connectivityManager.activeNetwork ?: return NetworkState()
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkState()
        return parseNetworkState(capabilities)
    }

    private fun parseNetworkState(capabilities: NetworkCapabilities): NetworkState {
        // NET_CAPABILITY_VALIDATED 确保网络有实际的互联网连接，而不仅仅是连接到路由器/门户
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        if (!isValidated) {
            return NetworkState(isConnected = false, type = NetworkType.NONE)
        }

        // 优先级: WiFi > Cellular > Ethernet > VPN
        val type =
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
                else -> NetworkType.NONE
            }

        return NetworkState(isConnected = true, type = type)
    }

    override fun close() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
