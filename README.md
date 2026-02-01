# Study Tracker

一个基于 Android Jetpack Compose 开发的简洁学习进度追踪应用。旨在帮助用户量化学习时间，管理多层级的学习项目（如课程、单元），并提供直观的统计分析。

## 主要功能

- **多级项目管理**：支持创建学习项目，并细分至章节或单元。
- **进度追踪**：记录每次学习时长，自动计算完成度。
- **数据统计**：内置图表分析功能，直观展示学习数据和趋势。
- **本地存储**：基于 Room 数据库实现，所有数据保留在设备本地，安全快捷。
- **现代 UI**：采用 Jetpack Compose 构建，支持深色模式。

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose / Material 3
- **本地数据库**: Room
- **状态管理**: ViewModel & Repository Pattern
- **持久化**: Preferences DataStore

## 如何构建

1. 克隆仓库：
   ```bash
   git clone https://github.com/FunMax-Edward/study-tracker.git
   ```
2. 使用 Android Studio (Ladybug 或更高版本) 打开工程。
3. 等待 Gradle 同步完成。
4. 运行 `./gradlew assembleRelease` 生成已签名的发行版（需配置密钥库）。

## 许可证

基于 Apache License 2.0 协议。
