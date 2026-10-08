# Báo cáo khôi phục — 09/10/2026

## A. Tổng quan

Đã áp dụng skill `restore-fabric-mod` tại
`C:/Users/Ethan/.codex/skills/restore-fabric-mod/SKILL.md`, đọc cả
`references/mappings-and-build.md`, `references/readiness-review.md` và
`scripts/compare_jars.py` trước khôi phục. Workflow: kiểm kê → sao lưu → decompile
→ dựng Gradle/mappings → sửa theo bytecode → compile/build → đối chiếu → đánh giá.

- Đầu vào: `D:/DonutUtil/DonutUtility-1.21.11-1.1.4-CLASSIC-QUICKMOVE-SELL-FIX12 (1).jar`.
- Bản sao bất biến: `D:/DonutUtil/restore-work/backup/original.jar`.
- SHA-256 gốc và bản sao: `b5f09a2c54f23449fdbf45a853c75cb53351611cde495cd4356f400b12779ac9`.
- Workspace ban đầu chỉ có JAR, không có source hoặc Git repository; không ghi đè project cũ.
- Decompiler: **Vineflower 1.11.2**, JAR từ Maven Central; kiểm tra SHA-512 công bố.
  CFR không cần dùng. [Nguồn Vineflower](https://github.com/Vineflower/vineflower/releases/).
- Phát hiện **67 class, 6 package**, decompile đủ thành **58 file Java** với 9 type
  lồng; không thấy marker method decompile thất bại. Một cảnh báo metadata
  `AutoSellLogic$State` không nhất quán được điều tra bằng `javap`.
- 6 resource file trong JAR; không có nested JAR, access widener hay service descriptor.
- Minecraft names dùng Intermediary. Tên class/method của mod còn đọc được;
  không có bằng chứng đủ để suy ra namespace phát triển ban đầu của tác giả.
- Source nguyên trạng từ Vineflower được giữ tại `../restore-work/decompiled`.

| Thành phần | Phiên bản/bằng chứng |
|---|---|
| Mod | `donututility` 1.1.4, client-only |
| Minecraft | 1.21.11, metadata gốc |
| Java | JDK 21.0.9 JetBrains tại `D:/Android Studio/jbr`; classfile major 65 |
| Gradle | 9.4.0, wrapper chính thức; wrapper JAR và distribution có SHA-256 |
| Loom | 1.16.3 |
| Fabric Loader | build pin 0.18.4; metadata gốc cho phép ≥0.18.1 |
| Fabric API | build pin 0.141.3+1.21.11; source legacy có imports API |
| MaLiLib | build pin 0.27.14; metadata gốc yêu cầu `>=0.27.14- <0.28.0-` |
| Mappings | Intermediary 1.21.11 Tiny v2 với cột named bằng intermediary |

Các build pin là bộ dependency đã build được, không được coi là phiên bản
chính xác tác giả đã dùng. Manifest JAR gốc chỉ ghi Created-By 21.0.11 (Debian).
MaLiLib 0.27.14 có manifest Loom 1.16.3; Loom 1.14.10 ban đầu từ chối nó.
[Module metadata chính thức](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.16.3/fabric-loom-1.16.3.module)
chỉ định Java 21 và Gradle plugin API 9.4.0. MaLiLib yêu cầu networking API
≥5.1.4 và resource loader; Fabric API đã resolve networking 5.1.5+ae1e07683e.

Entrypoint giữ nguyên `dev.autoominous.AutoOminousMod` (main). Cấu hình
`autoominous.mixins.json` giữ 8 mixin client: MinecraftTick, AutoTpaTick,
MessageHandler, DisconnectTick, DisconnectConnectScreen, DisconnectDisconnectedScreen,
StatsTick và StatsHud. Không thêm entrypoint legacy `SeaatsClient`.

## B. Kết quả có bằng chứng

| Kiểm tra | Kết quả |
|---|---|
| Wrapper `--version` | PASS, exit 0, Gradle 9.4.0/JDK 21.0.9 |
| `compileJava --rerun-tasks` cuối | PASS, exit 0; compile thực sự chạy |
| `build` cuối | PASS, exit 0; jar, sourcesJar, remapJar, remapSourcesJar chạy |
| Bộ JUnit trong Gradle | Không có: `test NO-SOURCE`, không coi là test pass |
| Test parser cô lập | PASS, 13 ca đối chiếu original/rebuilt, cả kiểu boxing và lỗi input |
| Test số học cô lập | PASS, 112 ca đối chiếu original/rebuilt, gồm biên long/int |
| Kiểm kê class/package | PASS: 67/67 class, 6/6 package, không thêm/bớt |
| Method/field không synthetic | PASS: không thiếu; so sánh cả private descriptors |
| Enum constants/thứ tự | PASS: không đổi |
| Resources | 5/6 byte-identical; chỉ manifest thay đổi |
| Metadata/entrypoint/mixin/assets/license | PASS: giữ nguyên bytes |
| Namespace đầu ra | PASS: manifest `intermediary`, descriptors giữ tên `class_*` |
| `vscode` | PASS, exit 0; sinh config và tải development assets |
| `runClient --dry-run` | PASS, exit 0; không thực thi game |
| IDE hết lỗi/import/debug | NOT TESTED |
| Minecraft khởi động/mod load | NOT TESTED |
| Automation, HUD, GUI trong game | NOT TESTED |

125 ca kiểm tra cô lập chỉ lấy các method tính toán/parser đã chọn, dùng
scaffolding trơ và thời gian/cấu hình giả. Không khởi tạo mod, Minecraft,
webhook, network hay giao dịch. Đây không phải 125 test chức năng toàn mod.

Python không khả dụng trên PATH, nên dùng công cụ Java/ASM chỉ đọc archive
`../restore-work/tools/CompareArtifacts.java` thay cho helper Python của skill.
Tất cả 67 class khác bytes sau biên dịch lại, không suy ra thay đổi chức năng
từ điều này. Các khác biệt call/write sequence do bố trí nhánh, compiler,
interface/concrete collection và sửa nguồn đã được phân loại; không chứng minh
tương đương bytecode/toàn bộ control flow. Lambda/bridge synthetic không được
đòi tên giống hệt giữa hai compiler.

Manifest mới ghi Gradle/Loom/Loader/mappings thay cho Created-By cũ; không có
ngữ nghĩa entrypoint phụ thuộc manifest bị mất. Không đóng gói dependency mới
vào JAR mod. Dependencies được resolve bằng Gradle, không dùng build scan.

## C. Sửa source và phát hiện

Lần compile đầu đầy đủ báo **48 lỗi**; sau khi sửa xuất hiện thêm **1 lỗi lambda**.
Đã xử lý **49 diagnostic lỗi compiler** và **3 warning reflection-varargs**.
Đây không phải 49 lỗi logic độc lập. Hiện còn ghi chú deprecated/unchecked;
chưa audit đầy đủ từng warning bằng `-Xlint`.

**14 file Java khác bản Vineflower**, còn 44 file giữ nguyên:

| File | Khôi phục |
|---|---|
| `AutoSellLogic` | Generic List player; thứ tự khởi tạo 2 string; 5 khai báo về package access |
| `HumanizedSellRuntime` | Boolean → 0/1 cho phép toán; dùng Long.compare tương đương LCMP, không trừ long gây overflow |
| `SeaSettingsScreen` | Generic Map, capture chỉ số tab final, phục hồi Integer/Double boxing |
| `DisconnectRuntime` | Generic List player |
| `DisconnectSettings` | Generic List string và thứ tự initialized |
| `SwordRecoveryRuntime` | Generic List Text tooltip |
| `SlashSellSettings` | Thứ tự khởi tạo toggle, final list được gán một lần; giữ việc tạo list cũ |
| `DeathRecoveryRuntime` | Reflection truyền đúng một đối số null |
| `HomePositionGuardRuntime` | Reflection truyền đúng một đối số null |
| `SlashSellRuntime` | Reflection truyền đúng một đối số null |
| `AutoReconnectSettings` | initialized=true sau loadFromDisk/snapshot như bytecode gốc |
| `AutoTpaSettings` | initialized=true sau loadFromDisk/snapshot |
| `BountyHudSettings` | initialized=true sau loadFromDisk/snapshot |
| `StatsSettings` | initialized=true sau loadFromDisk/snapshot |

Các file Java nằm dưới `src/main/java/com/seastore/auto/seaats/client` hoặc
`src/main/java/dev/autoominous` theo package tương ứng. Danh sách đường dẫn đầy đủ:
`../restore-work/logs/changed-java-files.txt`.

**Thay đổi quyền truy cập đã được người dùng đồng ý riêng:** JAR gốc có
HumanizedSellRuntime trực tiếp truy cập private member của AutoSellLogic, không
có nest metadata hợp lệ. Đổi `tickDelay`, `currentState`, `isSellGuiOpen`,
`isProtectedNeverSellItem` và enum `State` sang package access. Không đổi branch,
timer hoặc thứ tự enum. Đây là sửa bất nhất vốn có của JAR, không chỉ formatting.

**Giữ nguyên có chủ ý:** `isHumanizedSellV2Enabled()` và
`isBulkSameItemTransferEnabled()` trả false trong bytecode gốc; một số helper
trả hằng/không làm việc. Không bật lại tính năng dựa trên tên setting. Các class
SeaATS legacy, payment/webhook vẫn được giữ; `SlashSellRuntime.syncSeaConfig`
vô hiệu hóa nhiều đường legacy giống JAR gốc.

| Mức độ | Phát hiện/giới hạn và hướng xử lý |
|---|---|
| Cao, đã sửa với đồng ý | Private access bất nhất ở AutoSellLogic/HumanizedSellRuntime; vẫn cần test chạy trong game |
| Cao, chưa xác minh runtime | Bounty/Home/Death/Sword/Vex chia sẻ trạng thái và quyền GUI. Test timeout, disconnect, tắt/bật giữa chu kỳ, resume trước khi mở rộng automation |
| Trung bình | TPA/Bounty/Sword nhận diện bằng tên GUI/chat/tooltip tiếng Anh và item. Test đúng định dạng server và locale mục tiêu |
| Trung bình | Nhiều reflection và catch Throwable bỏ qua lỗi; build không phát hiện lỗi tên runtime. Kiểm tra log trong client thử nghiệm |
| Trung bình | Metadata Loader/API khá rộng hoặc không khai báo API trực tiếp, trong khi bộ build pin hẹp. Chưa chứng minh toàn bộ range; giữ metadata gốc theo yêu cầu |
| Trung bình | Source Intermediary, biến varN và thiếu regression suite toàn state machine làm tăng chi phí bảo trì |

## D. Mức sẵn sàng

**PARTIALLY READY. Có thể chỉnh sửa, thêm class/module và build JAR ngay.**

| Nhu cầu | Đánh giá |
|---|---|
| Thêm class/module | Sẵn sàng về source/build |
| Thêm chức năng | Có điểm mở rộng; cần kiểm thử ưu tiên và state machine |
| Mở rộng Settings GUI | Có GUI MaLiLib; parser legacy đã kiểm tra cô lập |
| Thêm keybind | Có provider/map/category; cần đăng ký đủ |
| Thêm HUD | Có StatsHudMixin, cần kiểm tra render thực tế |
| Thêm cấu hình | Có JSON/Properties và các handler; cần nối save/load/validation |
| Development client | Task/launch config/assets sẵn; khởi động chưa kiểm thử |
| Debug VS Code | Config đã sinh; Gradle import/breakpoint chưa kiểm thử |
| Build sau chỉnh sửa | Đã chứng minh qua nhiều lần compile/build |

Chưa kết luận READY cho thay đổi automation phức tạp hay phát hành, do còn
reflection, trạng thái tương tác và chưa có kiểm thử runtime. Build thành công
không đồng nghĩa mod load hay mọi tính năng hoạt động đúng.

## E. Đầu ra và bước tiếp theo

- Project: `D:/DonutUtil/restored-donututility`.
- Source: `src/main/java`; tài nguyên: `src/main/resources`.
- Runtime JAR: `build/libs/donututility-1.1.4.jar`.
- SHA-256 runtime JAR: `a26a0cf98834b6e468f6b3bb33547866d2e85128b515dc49f869954162f47d07`.
- Source JAR: `build/libs/donututility-1.1.4-sources.jar`.
- Đã tạo `settings.gradle`, `build.gradle`, `gradle.properties`, wrapper đầy đủ,
  `.gitignore`, `.vscode/launch.json`, README và báo cáo này.
- Bằng chứng cuối ở `../restore-work/logs`: `compile-final.log`, `build-final.log`,
  `artifact-comparison-final.txt`, `parser-differential-test.log`,
  `arithmetic-differential-test.log`, `vscode.log`, `runClient-dry-run.log`.
- Tools/baseline/logs nằm riêng trong `../restore-work`; build/cache/assets được
  Gradle sinh trong project và Gradle user home hiện có. Không xóa cache người dùng.

1. Làm theo README để import Gradle bằng JDK 21 trong VS Code.
2. Chủ động chạy client profile riêng; xác minh mod load, 8 mixin và GUI/keybind/HUD.
3. Kiểm tra cấu hình save/load, giá trị biên, bật/tắt và reload.
4. Trước thêm automation, lập test cho tranh chấp GUI, timeout, disconnect,
   death/home/sword/Vex và sự kiện chat; dùng môi trường kiểm thử kiểm soát được.
5. Compile/build sau mỗi thay đổi và đối chiếu artifact; chỉ đánh giá sẵn sàng
   phát hành sau khi có kết quả runtime.

**PROJECT ĐÃ SẴN SÀNG ĐỂ VIẾT THÊM CHỨC NĂNG CHƯA?** Có về source và build;
**PARTIALLY READY** về phát triển toàn diện, còn bước xác minh IDE/runtime và
regression các cơ chế automation tương tác.
