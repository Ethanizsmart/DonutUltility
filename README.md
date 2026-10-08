# Donut Utility — source khôi phục

Project Fabric cho Minecraft **1.21.11**, mod ID `donututility`, phiên bản **1.1.4**.
Source giữ tên Minecraft **Intermediary** (`class_*`, `method_*`, `field_*`).

## Build trên máy hiện tại

Mở PowerShell trong thư mục chứa README này:

```powershell
$env:JAVA_HOME = 'D:\Android Studio\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat --version
.\gradlew.bat compileJava --rerun-tasks
.\gradlew.bat build
```

Trên máy khác, đặt `JAVA_HOME` tới JDK 21 đã cài. Không cần cài Gradle toàn cục.
Wrapper tải Gradle 9.4.0 và kiểm tra SHA-256 của distribution.
Không dùng Java 26 trên PATH làm compiler mặc định của project này.

JAR runtime: `build/libs/donututility-1.1.4.jar`.
JAR source: `build/libs/donututility-1.1.4-sources.jar`.
Không dùng JAR trong `build/devlibs` để cài mod.

Dependencies được ghim trong `build.gradle`: Loader 0.18.4, Fabric API
0.141.3+1.21.11, MaLiLib 0.27.14; plugin Loom 1.16.3.
Dependencies Minecraft cung cấp như Gson, Brigadier, JOML không bị đóng gói trùng.

## VS Code và development client

Mở **chính thư mục này** bằng VS Code, import dưới dạng Gradle project.
Cần hỗ trợ Java và Java Debugger. Chọn JDK 21 cho Gradle và project runtime,
rồi đợi Gradle import hoàn tất. Các tên `class_*` là chủ ý, không thay imports
bằng tên Yarn/Mojang chỉ để loại bỏ lỗi IDE.

```powershell
.\gradlew.bat vscode
```

Lệnh trên đã chạy thành công, sinh `.vscode/launch.json`, launch config và tải
assets development. Sinh lại nếu chuyển đường dẫn project. Cấu hình **Minecraft
Client** có thể dùng để đặt breakpoint và debug sau khi IDE import thành công.
Cấu hình server do Loom sinh mặc định không phải mục tiêu của mod client-only này.

Khi chủ động muốn mở game trong profile development:

```powershell
.\gradlew.bat runClient
```

**Chưa chạy game/debug thực tế.** Phiên khôi phục chỉ chạy `runClient --dry-run`.
Để kiểm thử tiếp, dùng profile riêng tại `run`, cấu hình giả và môi trường kiểm
thử kiểm soát được; kiểm tra GUI/save/load trước khi thử automation. Không chép
cấu hình tài khoản hoặc webhook thật vào profile kiểm thử.

## Điểm mở rộng

| Công việc | Vị trí bắt đầu |
|---|---|
| Class/module mới | `src/main/java/dev/autoominous` |
| Khởi tạo | `AutoOminousMod`, `AutoOminousInitHandler` |
| Settings GUI/tab | `gui/AutoOminousGuiConfigs` và lớp `*Settings` tương ứng |
| Keybind | `event/AutoOminousInputHandler`, `AutoOminousSettings` |
| HUD | `mixin/StatsHudMixin`, `StatsRuntime`, `StatsProfitRateRuntime` |
| Bounty | `BountyRuntime`, `BountyMinecraftBridge`, `BountySettings` |
| Auto sell | `AutoSellRuntime`, `SlashSellRuntime`, `AutoSellLogic`, `HumanizedSellRuntime` |
| Phục hồi/an toàn | `DeathRecoveryRuntime`, `SwordRecoveryRuntime`, `HomePositionGuardRuntime`, `VexSafetyRuntime` |

Một setting mới cần đủ khai báo → GUI → validation → save/load → nơi sử dụng.
Keybind cần thêm vào map và danh mục. Một automation mới phải tôn trọng quyền
sử dụng container và các điều kiện chặn của Bounty/Home/Death/Sword/Vex; không
chỉ thêm một callback tick độc lập.

Giữ metadata `fabric.mod.json` và danh sách mixin đồng bộ nếu thêm entrypoint
hoặc mixin. Sau chỉnh sửa, compile/build lại và kiểm tra riêng hành vi vừa đổi.

Xem [RESTORATION_REPORT.md](RESTORATION_REPORT.md) để biết bằng chứng, giới hạn,
các thay đổi source và mức sẵn sàng phát triển.
