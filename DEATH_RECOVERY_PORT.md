# CoreV2 1.1.8 — Death Recovery và Auto Buy Sword từ release 1.1.7

Bản này tiếp tục dùng source CoreV2 đã khôi phục; chỉ tích hợp luồng hồi sinh
và mua kiếm được yêu cầu. **Giữ luồng `/sell` của CoreV2 hiện tại.**
Không dùng `AutoSellLogic` hay quy tắc bán inventory của release làm bản thay thế.

## Hành vi

- Death Recovery: phát hiện chết → chờ Respawn Delay → hồi sinh → chờ Home Delay
  → gửi lệnh Home 1 đã cấu hình → chờ 8 giây → chạy lượt bán ưu tiên bằng bộ bán CoreV2
  → đợi giao diện và container server đóng, hết vật phẩm mà bộ bán hiện tại nhận là
  có thể bán → trả quyền cho automation. Lượt này chạy cả khi Auto Sell đang OFF;
  giá trị công tắc của người dùng được giữ. Nếu không có đồ có thể bán thì bỏ qua.
- Auto Buy Sword có công tắc riêng, hoạt động kể cả khi Death Recovery OFF.
  Thiếu Diamond Sword Looting III liên tục đủ 5 giây thì bắt đầu mua khi GUI rảnh.
  Tìm qua `/ah sword looting 3`, lọc giá thấp nhất, chọn ứng viên hợp lệ trên trang
  đầu với giá tối đa 5.000.000, kiểm tra kiếm/enchantment/giá ở bước xác nhận,
  rồi chuyển kiếm về hotbar. Có kiếm sẵn thì không mua thêm.
- Nếu cần chỗ trống để nhận kiếm, gọi cùng bộ bán CoreV2, giữ các quy tắc bảo vệ
  đồ hiện tại. Đồ mà CoreV2 bảo vệ không trở thành đồ được bán theo release.
- Dừng mua khi đang hồi sinh; phối hợp với Home Guard, Vex, Bounty, TPA và command
  backoff hiện có. Reset phiên cũ khi thay player/world hoặc mất kết nối.
- HUD CoreV2 và các mixin cũ vẫn có mặt.

Tên API cũ `MissingSwordWatchdog*` được giữ làm lớp chuyển tiếp tới bộ mua mới;
không còn chạy đồng thời hai bộ mua 30 giây và 5 giây. Giá trị Auto Buy Sword dùng
property hiện có `recoverSwordAfterDeath` trong cấu hình Death Recovery.
File cấu hình watchdog 30 giây cũ không được dùng để điều khiển công tắc mới.
Không sửa file cấu hình game thật trong quá trình build/kiểm tra.

## Nguồn và baseline

Nguồn donor: [release v1.1.7](https://github.com/Ethanizsmart/DonutUltility/releases/tag/v1.1.7),
tag commit `919446412944717759411d33103674dcb778d424`.
Đã kiểm tra SHA-256 asset tải về với digest GitHub API:

| Asset | SHA-256 |
|---|---|
| donor runtime JAR | `5c6edaae5b4f4abd8fda795fe751a87f66dc40404ce8c795f9e78f95e70426d8` |
| donor sources JAR | `3f37b4bf19e0fc60dba341246f453297a442c3e939a1f0deae37ce2f3ce6e954` |
| CoreV2 trước tích hợp | `970e7728f9f4915e4079e424fedcd530bd80108006238fd41f5042342a40c743` |
| CoreV2 release 1.1.8 | `97b49f8c6684aeb348d1e4af0c1fcc7039c2caff77cb8d6548ff99aeed26e52b` |

Tại workspace gốc, baseline/source donor và bằng chứng nằm trong
`../death-recovery-port/{baseline,donor,logs,tests}`.
Baseline khôi phục ban đầu vẫn nằm trong `../restoration/`.
`RESTORATION_REPORT.md` mô tả giai đoạn khôi phục trước khi thêm hai tính năng này;
kết quả đối chiếu toàn bộ 146 lớp trong báo cáo đó không phải phép kiểm tra bảo toàn
toàn bộ hành vi của bản tích hợp.

## Điểm chỉnh sửa

- `DeathRecoveryRuntime` và `DeathRecoverySettings`: luồng hồi sinh/Home/lượt bán.
- `RecoverySellBridge`: cấp quyền tick tạm thời cho bộ bán hiện tại, không thay
  thuật toán bán hoặc công tắc lưu cấu hình; chờ đúng trạng thái IDLE và đóng menu.
- `SwordRecoveryRuntime`, `MissingSwordTimer`, `AuctionSwordRules`: mua kiếm độc lập.
- `AutomationCoordinator`, `TransactionGate`, `MinecraftTickMixin`: thứ tự tick,
  quyền GUI và tránh chạy bộ mua cũ lần thứ hai.
- `G232SafeRuntime`: xóa trạng thái khôi phục kiếm tồn đọng khi bắt đầu lần chết mới.
- GUI và `MissingSwordWatchdog*`: nhãn 5 giây và chuyển tiếp về công tắc mới.
- `DialogScreenAccessor`: đọc dialog xác nhận. Đã đối chiếu descriptor của
  `class_11473.field_60761` là `class_11419` trong Minecraft 1.21.11.

## Kiểm chứng

- Gradle 9.4.0, launcher/daemon Java 21.0.9, Loom 1.16.3;
  `compileJava --rerun-tasks` và `build` đều exit 0.
- `verifyAuctionSwordRules`: **54 checks** về mốc thiếu kiếm 5 giây, giá, bộ lọc,
  chọn ứng viên và xác nhận. Task này tự chạy trong `build`/`check`.
- **35 checks** Death Recovery/cầu nối bán: respawn/home delay, 8 giây, Auto Sell
  OFF/ON, trả cờ khi lỗi, chờ screen/container đóng, không có đồ bán, chết lại,
  đổi player/connection, disconnect, tắt Death Recovery và Vex reset.
  Harness dùng hai class đã biên dịch thực, thay clock bằng ASM và fixture inert
  cho Minecraft/cấu hình/seller. Không chạy entrypoint, Minecraft hay mạng;
  phép kiểm tra này không xác nhận giao dịch trên server thật.
- Bảo toàn seller: **27 file nguồn và 34 class trong runtime JAR giống hệt baseline**,
  gồm toàn bộ `com/seastore`, `SlashSellRuntime/Settings`, `AutoSellRuntime`,
  `CompactSlashSellSettings`, `FarmCycleRuntime`, `OminousSurplusSellPolicy`
  và `ProtectedItemAutoUseRuntime` (kể cả lớp lồng nhau).
- Archive: 148 → **156 class**, Java major 65; không thiếu class/resource,
  không trùng entry, không header class lỗi. 8 class mới là auction rules/timer,
  bridge, dialog accessor và bản ghi listing. 11 class cũ đổi bytecode do tích hợp.
  Resource `autoominous.mixins.json` thay đổi để thêm accessor; `fabric.mod.json`
  đổi version thành 1.1.8 và cập nhật mô tả tính năng. Entrypoint và các resource
  còn lại giữ nguyên.

**Runtime Minecraft: NOT TESTED.** Chưa xác nhận mod load, GUI/dialog thực,
respawn/teleport hoặc bán/mua thực trên server. Source đã sẵn sàng chỉnh sửa và build;
khả năng hoạt động trong game còn cần thử trong môi trường game phù hợp.

## Build và file dùng

Mở thư mục dự án bằng IntelliJ/VS Code, import Gradle và chọn JDK 21.
Build theo `README.md` hoặc chạy `powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1`.
Kiểm tra rules riêng: `$env:JAVA_HOME='D:\Android Studio\jbr'; .\gradlew.bat verifyAuctionSwordRules`.
Runtime build ở `build/libs/DonutUtility-CoreV2-restored-1.1.8.jar`.

File bàn giao trong workspace gốc:
`../release-v1.1.8/donututility-1.1.8.jar` và
`../release-v1.1.8/donututility-1.1.8-source.zip`.
ZIP chứa Gradle wrapper, source/resource, kiểm tra rules, tài liệu và harness kiểm chứng;
không chứa cache, thư mục build/run hoặc cấu hình game.
