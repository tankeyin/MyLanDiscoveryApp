# APK 构建指南

## 方法一：使用 GitHub Actions（推荐，无需本地环境）

### 步骤：

1. **Fork 或上传项目到 GitHub**
   ```bash
   # 在项目目录初始化 git
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/你的用户名/MyLanDiscoveryApp.git
   git push -u origin main
   ```

2. **自动构建**
   - 项目已包含 `.github/workflows/build-apk.yml`
   - 每次 push 代码到 main 分支会自动触发构建
   - 构建完成后，在 Actions 页面下载 APK

3. **获取 APK**
   - 进入 GitHub 仓库 → Actions 标签
   - 点击最新的 workflow run
   - 在 Artifacts 部分下载 `app-debug.apk`

### GitHub Actions 构建输出：
- ✅ Debug APK (可直接安装测试)
- ✅ Release APK (未签名，需签名后发布)

---

## 方法二：使用 Android Studio（本地构建）

### 前提条件：
- 安装 [Android Studio](https://developer.android.com/studio)
- JDK 17 或更高版本

### 步骤：

1. **打开项目**
   ```
   File → Open → 选择 MyLanDiscoveryApp 文件夹
   ```

2. **等待同步**
   - Android Studio 会自动下载 Gradle 和依赖
   - 首次同步可能需要 5-10 分钟

3. **构建 APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

4. **获取 APK**
   - 构建完成后点击右下角提示
   - 或在 `app/build/outputs/apk/debug/` 找到 APK

5. **安装到手机**
   ```
   # 通过 adb 安装
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 方法三：使用命令行（需配置环境）

### 前提条件：
```bash
# 1. 安装 JDK 17
java -version  # 应显示 17.x.x

# 2. 安装 Android SDK
# 下载地址: https://developer.android.com/studio#command-tools

# 3. 设置环境变量
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### 构建步骤：

```bash
cd MyLanDiscoveryApp

# 赋予执行权限 (Linux/Mac)
chmod +x gradlew

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 输出位置
ls app/build/outputs/apk/debug/app-debug.apk
ls app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 签名 Release APK（发布到应用商店前必需）

### 生成签名密钥：
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

### 签名 APK：
```bash
# 使用 apksigner (推荐)
apksigner sign --ks my-release-key.jks --out app-signed.apk app-release-unsigned.apk

# 或使用 jarsigner
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.jks app-release-unsigned.apk my-alias
```

### 验证签名：
```bash
apksigner verify -v app-signed.apk
```

---

## 快速测试

### 使用模拟器：
```bash
# 启动模拟器
emulator -avd Pixel_7_API_34

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 查看日志
adb logcat -s MdnsService:D RingtoneServer:D
```

### 使用真机：
1. 开启开发者选项和 USB 调试
2. 连接电脑
3. 允许调试授权
4. 运行 `adb install app-debug.apk`

---

## 常见问题

### Q: Gradle 同步失败？
**A**:
- 检查网络连接（需要访问 maven.google.com）
- 尝试 File → Invalidate Caches → Invalidate and Restart
- 检查 Gradle 版本兼容性

### Q: 构建时出现 "SDK not found"？
**A**:
- 在 `local.properties` 中添加：
  ```
  sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
  ```

### Q: APK 安装失败？
**A**:
- Debug APK 可以直接安装
- Release APK 必须先签名
- 检查是否允许安装未知来源应用

---

## 预构建 APK 下载

如果你不想自己构建，可以：

1. **等待 GitHub Actions 构建完成**
   - 本项目配置了自动构建
   - 构建完成后 APK 会作为 Artifact 提供下载

2. **联系开发者获取**
   - 向项目维护者索取测试版 APK

---

## 下一步

构建成功后：
1. 在两台 Android 设备上安装 APK
2. 确保连接同一 Wi-Fi
3. 按照 [DEMO.md](DEMO.md) 进行测试
