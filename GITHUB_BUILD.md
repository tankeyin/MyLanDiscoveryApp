# GitHub 自动构建 APK 完整指南

无需安装 Android Studio，免费自动构建 APK！

---

## 前置条件

1. 一个 GitHub 账号（免费注册）
2. 本项目代码文件

---

## 步骤 1：注册 GitHub 账号

1. 访问 https://github.com/signup
2. 输入邮箱、密码、用户名
3. 验证邮箱
4. 完成注册

---

## 步骤 2：创建仓库

### 方法一：网页创建（推荐）

1. 登录 GitHub
2. 点击右上角 **+** → **New repository**
3. 填写信息：
   - **Repository name**: `MyLanDiscoveryApp`
   - **Description**: `局域网设备发现 Android App`
   - 选择 **Public**（免费）
   - 勾选 **Add a README file**
   - 点击 **Create repository**

### 方法二：命令行创建

```bash
# 安装 GitHub CLI: https://cli.github.com/
gh auth login
gh repo create MyLanDiscoveryApp --public --source=. --push
```

---

## 步骤 3：上传代码

### 方式 A：使用 Git 命令

```bash
# 进入项目目录
cd D:\desktop\code\test\MyLanDiscoveryApp

# 初始化 git
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 关联远程仓库（替换为你的用户名）
git remote add origin https://github.com/你的用户名/MyLanDiscoveryApp.git

# 推送代码
git branch -M main
git push -u origin main
```

### 方式 B：直接上传 ZIP

1. 压缩 `MyLanDiscoveryApp` 文件夹为 ZIP
2. 在 GitHub 仓库页面点击 **Uploading an existing file**
3. 拖放 ZIP 文件或选择文件
4. 点击 **Commit changes**

---

## 步骤 4：触发自动构建

推送代码后，GitHub Actions 会自动开始构建：

```
推送代码 → GitHub 检测到 .github/workflows/build-apk.yml → 开始构建
```

### 查看构建状态

1. 打开 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 查看构建进度（约 5-10 分钟）

构建流程：
```
✓ Checkout code
✓ Set up JDK 17
✓ Setup Android SDK
✓ Cache Gradle packages
✓ Build Debug APK
✓ Build Release APK
✓ Upload APK artifacts
```

---

## 步骤 5：下载 APK

### 方法一：从 Artifacts 下载（推荐）

1. 进入 **Actions** 页面
2. 点击最新的 workflow run（绿色 ✓）
3. 滚动到 **Artifacts** 部分
4. 下载：
   - `app-debug` - Debug 版本（可直接安装测试）
   - `app-release` - Release 版本（未签名）

### 方法二：从 Releases 下载

每次 push 到 main 分支会自动创建 Release：

1. 点击仓库页面的 **Releases**（右侧）
2. 找到最新的 release
3. 下载 APK 文件

### 方法三：手动触发构建

即使没有代码更改，也可以手动构建：

1. 进入 **Actions** 页面
2. 点击 **Build APK** workflow
3. 点击右侧 **Run workflow** → **Run workflow**
4. 等待构建完成

---

## 快速命令参考

### 首次设置
```bash
cd MyLanDiscoveryApp
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/用户名/MyLanDiscoveryApp.git
git push -u origin main
```

### 后续更新
```bash
git add .
git commit -m "Update"
git push
```

---

## 构建输出

| 文件 | 用途 | 安装方式 |
|------|------|----------|
| `app-debug.apk` | 测试版本 | 直接安装 |
| `app-release-unsigned.apk` | 发布版本（未签名）| 需签名后安装 |

---

## 常见问题

### Q: 构建失败？
**检查**：
1. 是否所有文件都已提交？
   ```bash
   git status
   ```

2. `.github/workflows/build-apk.yml` 是否存在？

3. 查看 Actions 日志中的具体错误

### Q: 如何签名 Release APK？
**步骤**：
1. 生成密钥：
   ```bash
   keytool -genkey -v -keystore my-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
   ```

2. 签名 APK：
   ```bash
   apksigner sign --ks my-key.jks --out app-signed.apk app-release-unsigned.apk
   ```

### Q: 构建太慢？
**优化**：
- 使用缓存（已配置）
- 只构建 Debug 版本：修改 workflow 删除 Release 构建步骤
- 使用私有 Runner（高级）

---

## 完整流程图

```
┌─────────────────┐
│  1. 创建GitHub账号 │
└────────┬────────┘
         ▼
┌─────────────────┐
│  2. 创建仓库      │
└────────┬────────┘
         ▼
┌─────────────────┐     ┌──────────────────┐
│  3. 上传代码      │────▶│ GitHub Actions   │
└────────┬────────┘     │ 自动触发构建      │
         │              └────────┬─────────┘
         │                       ▼
         │              ┌──────────────────┐
         │              │ 构建 Debug APK   │
         │              │ 构建 Release APK │
         │              └────────┬─────────┘
         │                       ▼
         │              ┌──────────────────┐
         └─────────────▶│ 下载 APK         │
                        └──────────────────┘
```

---

## 下一步

1. **获取 APK 后**：
   - 通过 USB 传输到手机
   - 或使用 `adb install` 命令安装

2. **测试应用**：
   - 参考 [DEMO.md](DEMO.md) 进行功能测试

3. **分享给朋友**：
   - 直接发送 APK 文件
   - 或分享 GitHub Release 链接

---

## 需要帮助？

- GitHub Actions 文档：https://docs.github.com/actions
- GitHub 支持：https://support.github.com
