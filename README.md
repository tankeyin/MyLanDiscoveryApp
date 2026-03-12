# 局域网设备发现 App (MyLanDiscoveryApp)

一个基于 mDNS 技术的 Android 应用，可以在同一局域网内发现和连接其他安卓设备，并支持远程响铃功能。

## 功能特性

- **设备发现**: 使用 mDNS (Bonjour/Zeroconf) 协议自动发现局域网内的其他设备
- **服务注册**: 将自己注册为可被发现的服务，让其他设备可以找到你
- **远程响铃**: 发现设备后，可以发送命令让对方的设备播放铃声（类似"查找我的手机"功能）
- **实时状态**: 实时显示设备连接状态和发现列表

## 技术栈

- **语言**: Kotlin
- **架构**: 原生 Android + Coroutines Flow
- **网络协议**:
  - mDNS/NSD (Network Service Discovery)
  - TCP Socket 通信
- **UI**: Material Design 3

## 项目结构

```
MyLanDiscoveryApp/
├── app/src/main/java/com/example/mdiscovery/
│   ├── service/
│   │   ├── MdnsService.kt      # mDNS 服务发现核心逻辑
│   │   ├── RingtoneServer.kt   # 响铃服务服务器端
│   │   └── RingtoneClient.kt   # 响铃服务客户端
│   └── ui/
│       ├── MainActivity.kt     # 主界面
│       └── DeviceAdapter.kt    # 设备列表适配器
├── app/src/main/res/
│   ├── layout/                 # 界面布局文件
│   ├── values/                 # 字符串、颜色等资源
│   └── mipmap/                 # 应用图标
└── build.gradle.kts            # Gradle 构建配置
```

## 核心组件

### 1. MdnsService
负责 mDNS 服务的注册和发现：
- `registerService(deviceName)`: 注册本设备服务
- `startDiscovery()`: 开始发现局域网内的设备
- `stopDiscovery()`: 停止发现
- `discoveredDevices`: Flow 实时推送发现的设备列表

### 2. RingtoneServer
TCP 服务器，监听响铃命令：
- 接收 `RING` 命令播放系统默认铃声
- 接收 `STOP` 命令停止播放
- 在后台协程中运行

### 3. RingtoneClient
发送响铃命令到其他设备：
- `sendRingCommand(host, port)`: 发送响铃请求
- `sendStopCommand(host, port)`: 发送停止请求

## 使用说明

### 准备工作

1. 确保所有设备连接到**同一个 Wi-Fi 网络**
2. 授予应用所需的网络权限（首次启动时会自动申请）

### 操作步骤

#### 第一步：注册自己的设备

1. 打开应用
2. 在"我的设备信息"区域输入设备名称（如"张三的手机"）
3. 点击【注册服务】按钮
4. 看到状态变为"已注册"表示成功

#### 第二步：发现其他设备

1. 点击【开始发现】按钮
2. 应用会扫描局域网内所有已注册的设备
3. 发现的设备会显示在下方的列表中

#### 第三步：让设备响铃

1. 在设备列表中找到目标设备
2. 点击设备右侧的【响铃】按钮
3. 对方的手机会立即播放铃声
4. 再次点击可以停止响铃

## 注意事项

- 部分路由器可能禁用 mDNS 广播，如果发现不了设备，请检查路由器设置
- Android 12+ 需要开启"附近设备"权限
- 响铃功能使用系统默认铃声，音量取决于设备的媒体音量设置

## 编译和安装

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 编译步骤

```bash
# 克隆项目后
cd MyLanDiscoveryApp

# 使用 Gradle 编译
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

或在 Android Studio 中直接点击 Run。

## 协议说明

### mDNS 服务类型
```
_myapp._tcp.
```

### Socket 通信协议
- **端口**: 8888
- **命令格式**: 纯文本 + 换行符
  - `RING\n` - 播放铃声
  - `STOP\n` - 停止铃声

## 扩展建议

可以基于此项目添加以下功能：
- 文件传输（基于建立的 TCP 连接）
- 文字消息聊天
- 设备认证和加密通信
- 设备分组管理

## 许可证

MIT License
