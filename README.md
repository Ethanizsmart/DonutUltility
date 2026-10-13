# Donut Utility CoreV2 — mã nguồn khôi phục

Khôi phục từ `DonutUtility-CoreV2-v0.2.35-G2.35-SWORD-INVENTORY-RECOVERY.jar`.
Đã biên dịch và build thành công cho Minecraft **1.21.11 / Java 21**.
Phiên bản release hiện tại trong `fabric.mod.json` là **1.1.8**; tên JAR gốc là nhãn bản vá riêng.

Bản hiện tại đã tích hợp **Death Recovery và Auto Buy Sword 5 giây** từ release
1.1.7 theo yêu cầu, **giữ luồng `/sell` CoreV2 hiện tại**. Chi tiết thay đổi,
89 kiểm tra đã qua và giới hạn thử nghiệm: [DEATH_RECOVERY_PORT.md](DEATH_RECOVERY_PORT.md).

Tải bản cài đặt và mã nguồn đầy đủ ở [release v1.1.8](https://github.com/Ethanizsmart/DonutUltility/releases/tag/v1.1.8).

## Mở để chỉnh sửa

Mở thư mục chứa README này bằng IntelliJ IDEA hoặc VS Code, rồi import/reload
dự án **Gradle**. Chọn **JDK 21** cho Gradle JVM. Máy hiện tại có JDK 21 tại
`D:\Android Studio\jbr`; Java mặc định trên PATH là 26, nên cần chọn đúng JDK.
Không mở từng file Java như một dự án độc lập.

Mã cần chỉnh nằm trong `src/main/java`; tài nguyên và metadata ở `src/main/resources`.
Dự án giữ namespace Intermediary của JAR gốc: `class_*`, `method_*`, `field_*`.
Đây là các tên Minecraft hợp lệ trong mô hình Gradle này.

## Build sau khi chỉnh sửa

Trong PowerShell tại thư mục dự án:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1
```

Script dùng JDK 21 cho lượt build, sau đó trả lại biến môi trường cũ.
Nếu chuyển sang máy khác:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1 -JdkHome 'C:\path\to\jdk-21'
```

Hoặc dùng wrapper trực tiếp:

```powershell
$env:JAVA_HOME = 'D:\Android Studio\jbr'
.\gradlew.bat build
```

JAR chạy trong game:
`build/libs/DonutUtility-CoreV2-restored-1.1.8.jar`.
File `*-sources.jar` chứa mã nguồn, không phải mod để bỏ vào `mods`.

Build được pin: Gradle 9.4.0, Loom 1.16.3, Loader 0.18.4,
Fabric API 0.141.3+1.21.11, MaLiLib 0.27.14.
Đây là bộ phiên bản đã build thành công, không phải khẳng định phiên bản công cụ
mà tác giả JAR ban đầu đã dùng. Minecraft cần các dependency mod tương ứng.

## Các điểm bắt đầu chỉnh mod

| Muốn chỉnh | File/thư mục |
|---|---|
| Đăng ký mod và cấu hình | `dev/autoominous/AutoOminousMod.java`, `AutoOminousInitHandler.java` |
| Giao diện cấu hình, tab | `dev/autoominous/gui/AutoOminousGuiConfigs.java` |
| Phím tắt | `dev/autoominous/event/AutoOminousInputHandler.java`, `AutoOminousSettings.java` |
| Tick, chat, HUD, kết nối | `dev/autoominous/mixin/` |
| /sell và nhịp thao tác | `SlashSellRuntime.java`, `SlashSellSettings.java`, `com/seastore/auto/seaats/client/AutoSellLogic.java` |
| Hồi sinh và mua kiếm/AH | `DeathRecoveryRuntime.java`, `RecoverySellBridge.java`, `SwordRecoveryRuntime.java`, `AuctionSwordRules.java`, `MissingSwordTimer.java` |
| Logic điều phối CoreV2 | `dev/autoominous/v2/core/`, `v2/broker/`, `v2/job/`, `v2/service/`, `CoreV2RuntimeBridge.java` |
| Dịch giao diện | `src/main/resources/assets/autoominous/lang/` |

Một tùy chọn mới cần có giá trị mặc định, binding GUI, lưu/đọc cấu hình và chỗ sử dụng.
Khi thêm automation, kiểm tra các broker/guard để tránh gửi lệnh hoặc thao tác
inventory trùng với luồng đang chạy. `CoreV2RuntimeBridge` có luồng shadow;
đọc đường gọi hiện có trước khi bật thêm hành động.

## Giới hạn khôi phục

JAR gốc thiếu `CoreV2RuntimeBridge$AnchorStatus` và `$SwordStatus`.
Hai lớp dữ liệu này đã được dựng lại với sự đồng ý của bạn, từ constructor,
descriptor và các trường mà bytecode sử dụng. Giá trị sentinel được suy luận:
`OUTSIDE = (false, 0L)`, `EMPTY = (false, false, false)`; không có bytecode gốc
để chứng minh chính xác initializer của hai lớp thiếu.

Đã sửa lỗi generics, boolean arithmetic, thứ tự khởi tạo static, numeric boxing
và reflection varargs do decompile. Không bật lại các getter cố ý trả `false`
trong bản gốc. Bản gốc và baseline nguyên trạng nằm cạnh dự án, trong
`../restoration/original/` và `../restoration/baseline/`.

Kiểm tra ở giai đoạn khôi phục trước khi thêm tính năng: compile/build, đối chiếu 146 lớp gốc và chữ ký thành viên,
thứ tự enum, annotations sau chuẩn hóa biểu diễn Mixin, 28 trường hợp parser,
300 trường hợp phép toán và lời gọi đóng màn hình với một tham số null.
`test NO-SOURCE` của Gradle không phải một bộ unit test đã chạy.

**Chưa chạy Minecraft, chưa kiểm tra mod load, GUI/HUD hoặc automation trên server.**
Chi tiết và bằng chứng: `RESTORATION_REPORT.md` và `../restoration/logs/`.
