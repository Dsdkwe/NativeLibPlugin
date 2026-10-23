# PanVK Mali-G925 Android Wrapper & Minecraft Vulkan 实践总结

这是一个关于在 Android 设备（搭载 Mali-G925 / PAN_ARCH 13）上，如何通过打包开源 PanVK 驱动，并在 Zalith Launcher (ZL2) 与 Fold Craft Launcher (FCL) 中强行调用，最终在 Minecraft 26.3+ (Vulkan) 上成功运行的硬核实践记录。

## 🏆 最终运行成绩
- **Minecraft 26.3 (原生 Vulkan / RenderPearl)**
- **纯血 PanVK 驱动**：稳定运行，平均帧率达到 **490+ FPS**（使用了 Vulky 模组进行软件模拟支持）。
- **MobileGL (OpenGL ES 后端)**：平均帧率达到 **600+ FPS**（得益于原厂闭源驱动的极限释放）。

## 📦 仓库内容与构建
本仓库通过 GitHub Actions 自动将 PanVK 源码编译并打包为 Android 原生库插件（APK），以适配支持原生库插件机制的启动器（如 ZL2）。

- **核心驱动**: `libvulkan_panfrost.so` (Mesa 26.3-devel 实验分支)
- **伴生库**: `libdrm.so`, `libc++.so`
- **配置引导**: `panfrost_icd.aarch64.json`
- **构建流程**: 通过 `.github/workflows/build.yml` 在云端进行全自动编译与 APK 打包（已解决 Kotlin 包名与资源冲突问题）。

## 🛠️ 踩坑与核心技术点记录

### 1. 为什么 ZL2 识别不到“驱动器”？
Zalith Launcher 的「Vulkan 驱动器」列表只识别专为其定制的 Turnip/PurpleVK 包。PanVK 只能通过「原生库插件（NativeLibPlugin）」的方式被扫描并挂载。

### 2. GitHub Token 权限限制
在通过 Termux 使用 `git push` 推送包含 `.github/workflows/` 的代码时，报错 `refusing to allow a Personal Access Token to create or update workflow`。**原因**：生成 Personal Access Token (PAT) 时，必须勾选 `repo` 和 `workflow` 两个权限。

### 3. Android 的 `noexec` 限制（核心卡点）
将 `.so` 和 `.json` 文件放在手机共享存储（如 `/storage/emulated/0/Download/`）时，Android 系统会禁止执行动态库（NoExec 限制）。
**解决方案**：需要将驱动文件移动到启动器拥有完全读写和执行权限的私有目录，例如 FCL 的专属目录：
`/storage/emulated/0/Android/data/com.tungsten.fcl/files/panvk/`

### 4. 环境变量配置（VK_ICD_FILENAMES）
无法通过 Java 的 `-D` 参数来指定 Vulkan 驱动。必须在 FCL 的「环境变量」设置中，手动填入：
- **Key**: `VK_ICD_FILENAMES`
- **Value**: `/storage/emulated/0/Android/data/com.tungsten.fcl/files/panvk/panfrost_icd.aarch64.json`

### 5. 突破 Vulkan 扩展缺失导致的原生崩溃
MC 26.3 的 Vulkan 渲染后端（RenderPearl）强制要求支持 `VK_EXT_vertex_attribute_divisor`，而 PanVK 尚未支持此扩展。直接启动会报 `BackendCreationException: Device missing capabilities`。
**解决方案**：引入 **Vulky Stability** 模组。它会拦截 Vulkan 检查，用 CPU 软件模拟缺失的特性（包括 `fillModeNonSolid` 等），并成功欺骗 `vkCreateDevice`，让游戏得以通过 PanVK 渲染启动。

## 🚀 食用指南
1. 从 Release 或 Actions 中下载打包好的 APK 插件。
2. 安装插件并在启动器中信任其签名。
3. 将驱动文件放置于可执行目录，并在启动器中配置好 `VK_ICD_FILENAMES` 环境变量。
4. 搭配使用 **Vulky** 模组运行 Minecraft 26.3+。
5. ⚠️ **注意**：PanVK 仍处于实验阶段，部分高级特性可能渲染异常；高帧率下请注意手机散热。

## 💡 特别致谢
- 参考项目: `panvk-kbase-android-g925`, `Llerry/panvk-kbase-android`
- 模组支持: Vulky Stability