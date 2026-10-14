# Donut Utility — source khôi phục

Project Fabric cho Minecraft **1.21.11**, mod ID `donututility`, phiên bản **1.1.6**.
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

JAR runtime: `build/libs/donututility-1.1.6.jar`.
JAR source: `build/libs/donututility-1.1.6-sources.jar`.
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

Master automation: starts OFF on every game launch. Press F8 to toggle all configured automation. The master key can be changed in the Utility GUI (X+C), under Toggle All Automation. Turning OFF cancels pending work and releases item use; individual feature settings are preserved.

Auto sell follows the CoreV2 action reference in `SellActions-CoreV2-original-logic.zip`
and `SELL_ACTIONS_LOGIC_VI.md` in the workspace parent. It chooses nearby sources with
varying tick pauses and Shift-clicks exact item/component matches as a group, bounded
by the initial free capacity in the first 45 sell slots. After one left-click confirm,
it waits six ticks and polls the batch before continuing in the same menu or closing
the server container. A confirm click alone does not count as a server sale receipt.

Protected swords/items are retained in all 36 inventory slots. Exactly one Ominous
Bottle is retained, preferring the selected hotbar slot, then the other hotbar slots,
then main inventory. A reserved stack is split with left-click source, right-click
source, left-click empty sell slot; all surplus bottles may be sold. Inventory probes
and post-death completion checks use this same protection policy. Post-death selling
still runs even when regular Auto /sell is OFF.

`gradlew.bat verifySellActions` runs the production transfer/rhythm code with offline
Minecraft click fixtures and the production inventory/menu predicates. It checks grouped
transfers, capacity, components, bottle splitting/recovery, cursor safety, menu ownership,
single confirm, polling, continuation and timeouts. It is included in `build`; live
server timing and sale receipts still require an in-game test.

Sword recovery after death now uses `/ah sword looting 3` instead of `/order`.
After the home position settles, it checks for an existing Diamond Sword with actual Looting III.
If all 27 main inventory slots are occupied, configured Auto /sell may run until a main slot is free.
The Auction filter is the hopper in the 48th cell (slot ID 47). The active option is detected from the bright label color in the styled tooltip, while inactive options are dark gray. The recovery left-clicks only when Highest Price or Recently Listed is active. When Lowest Price is bright, it stops clicking and chooses the cheapest eligible sword on the sorted first page, with a hard price cap of $5,000,000. If colors are ambiguous or menu contents incomplete, it waits rather than cycling blindly.
Listings with missing, ambiguous or unsupported prices are skipped. Prices support $ amounts and K/M/B suffixes.
Purchase confirmation supports both container menus and the native Yes/No dialog titled "Are you sure you want to buy this?". The displayed sword must match the selected Looting III sword, and the displayed price must equal the selected price and remain at or below $5,000,000. For the native dialog, action buttons are found recursively inside the scrollable body and the real Yes button callback is invoked once; recovery then waits for the inventory update before proceeding. Missing or conflicting prices are not confirmed. Unrecognized confirmations are closed and the search retries.
If no suitable listing is found, menus time out, or purchase verification fails, the search repeats with a 2–3 second pause. Recovery finishes only after a verified Looting III Diamond Sword is in the hotbar.
`gradlew.bat verifyAuctionSwordRules` runs the offline price/filter regression checks (also included in `build`). Live debugger inspection verified the actual purchase dialog title, matching Looting III enchantments, $1.5M price, and Yes/No buttons nested inside the scrollable body. After the recursive button fix, the Yes callback completed and recovery entered VERIFY_PURCHASE. Auto-use debugging verified hotbar sword selection, use-key holding and repeat calls. End-to-end receipt was not captured in that debug session; the user subsequently confirmed the current version was working.

Hotbar refill (`HotbarRefillRuntime`): keeps hotbar slot 1 (Looting III sword), slot 2 (Ominous Bottle), and slot 3 (Firework Rocket). Opens player inventory screen, swaps items via `SWAP` after 50ms, closes screen, and enforces a 400ms cooldown. Absence detection for sword recovery is set to 15 seconds.

