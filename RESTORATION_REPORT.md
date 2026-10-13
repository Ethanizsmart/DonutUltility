# Báo cáo khôi phục Donut Utility CoreV2

Ngày: 13/10/2026. Workflow: skill `restore-fabric-mod`.

## A. Đầu vào và dự án

- Đầu vào: `D:/CoreV2/DonutUtility-CoreV2-v0.2.35-G2.35-SWORD-INVENTORY-RECOVERY.jar`.
- Bản sao đã kiểm tra hash: `D:/CoreV2/restoration/original/`, giữ nguyên JAR gốc.
- SHA-256 gốc: `8289cd8949dd6dbd9a07418250d24c17c5fd7b69e892b812c6150755fb66b5da`.
- Dự án chỉnh sửa: `D:/CoreV2/DonutUtility-restored/`.
- Mã decompile chưa sửa và tài nguyên: `D:/CoreV2/restoration/baseline/`.
- Không có source/build cũ trong workspace trước khi khôi phục; không có Git repository cũ.
- Metadata: mod ID `donututility`, version `1.1.7`, client environment,
  main entrypoint `dev.autoominous.AutoOminousMod`, 9 client mixin.
- Minecraft 1.21.11; class major 65 (Java 21); không có nested dependency JAR,
  access widener hoặc chữ ký JAR trong đầu vào.
- Gốc: 146 class, 10 package, 39 resource. Decompile: 126 file Java,
  các lớp nested/anonymous nằm chung file lớp ngoài. Rebuild: 148 class,
  gồm toàn bộ 146 class gốc và 2 class dữ liệu bổ sung đã được duyệt.

Decompiler: Vineflower **1.12.0**, không dùng CFR fallback.
Nguồn tải: [Maven Central](https://repo.maven.apache.org/maven2/org/vineflower/vineflower/1.12.0/).
Đã đối chiếu SHA-512 với checksum công bố.
SHA-256 decompiler: `1dfcfe974395734fa467ce620661c7623d05ba83670de0529b1fbd63ff548b9d`.
Decompiler chạy bằng Oracle Java 26.0.1, exit 0; compiler/build dùng JDK 21.
Cảnh báo decompiler: InnerClasses metadata của `AutoSellLogic$State` không nhất quán
giữa owner và caller, và thiếu hai nested class của `CoreV2RuntimeBridge`.
Không thấy marker phương thức decompile thất bại; các class gốc đều có trong rebuild.

Lệnh decompile:

```powershell
java -jar D:\CoreV2\restoration\tools\vineflower-1.12.0.jar D:\CoreV2\restoration\original\DonutUtility-CoreV2-v0.2.35-G2.35-SWORD-INVENTORY-RECOVERY.jar D:\CoreV2\restoration\baseline
```

## B. Build và kiểm tra

| Hạng mục | Kết quả |
|---|---|
| Gradle wrapper `--version` | PASS, exit 0 |
| `compileJava --rerun-tasks` lần 1 | FAIL, 35 compiler errors, 4 null-varargs warnings |
| `compileJava --rerun-tasks` sau sửa | PASS, exit 0, task thực sự chạy |
| `build` | PASS, exit 0; jar, sourcesJar, remapJar, remapSourcesJar chạy |
| Script `build.ps1` | PASS, exit 0; build tiếp theo up-to-date |
| Gradle unit tests | `NO-SOURCE`; không có unit test suite của mod |
| So sánh archive và resource | PASS, không mất class/resource |
| Chữ ký và access flags của member không synthetic | PASS cho 146 class gốc |
| Thứ tự enum | PASS cho toàn bộ class gốc |
| Annotation class/field/method | PASS sau hai chuẩn hóa biểu diễn Mixin bên dưới |
| Differential parser và arithmetic | PASS với scaffolding giới hạn bên dưới |
| Import/debug trong IDE | NOT TESTED; hướng dẫn thiết lập trong README |
| Khởi động Minecraft / load mod / GUI / HUD / tính năng server | NOT TESTED |

Toolchain đã dùng: JDK 21.0.9 JetBrains tại `D:/Android Studio/jbr`
cho launcher, daemon và Java 21 toolchain; Gradle 9.4.0; Loom 1.16.3;
Fabric Loader 0.18.4; Fabric API 0.141.3+1.21.11; MaLiLib 0.27.14.
Đây là build pins được xác minh, không xác định được phiên bản build gốc của tác giả.

MaLiLib được đối chiếu checksum theo metadata Modrinth. Manifest dependency
ghi Loom 1.16.3; module metadata chính thức của Loom yêu cầu Java 21 và Gradle API
9.4.0. Dùng wrapper chính thức lấy từ tag Gradle v9.4.0; checksum wrapper
`55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c`
đã khớp checksum Gradle công bố. Distribution SHA-256 pin trong wrapper đã đối chiếu:
`60ea723356d81263e8002fec0fcf9e2b0eee0c0850c7a3d7ab0a63f2ccc601f3`.
Nguồn: [Loom module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.16.3/fabric-loom-1.16.3.module),
[Gradle wrapper checksum](https://services.gradle.org/distributions/gradle-9.4.0-wrapper.jar.sha256),
[Gradle distribution checksum](https://services.gradle.org/distributions/gradle-9.4.0-bin.zip.sha256).

Mapping giữ Intermediary: lấy Tiny v2 của Minecraft 1.21.11 và tạo cột `named`
giống hệt cột `intermediary` bằng mã tái lập trong `build.gradle`.
JAR runtime cuối được Loom remap và khai báo namespace `intermediary`.
Không tắt mixin/remapping, không loại bỏ class, không thêm class binary vào resources.

Compiler còn notes về deprecated API và raw/unchecked generics.
Cảnh báo cấu hình Loom lần đầu về remap `modifiers` xuất hiện khi setup dependency;
không phải compiler error; các build tiếp theo thành công.

## C. Các sửa chữa và bằng chứng

| Source | Thay đổi |
|---|---|
| `CoreV2RuntimeBridge.java` | Dựng lại `AnchorStatus(inside, stableMs)` và `SwordStatus(hotbar, inventory, freeSlot)` từ descriptor và field references trong bytecode. Người dùng đã duyệt. Sentinel suy luận `OUTSIDE(false,0L)`, `EMPTY(false,false,false)`; initializer gốc không thể phục hồi vì class không có trong JAR. |
| `AutoSellLogic.java` | Khôi phục list player generic. `State` dùng package visibility ở source để khớp classfile package access và caller trong cùng package. InnerClasses metadata cũ ghi private nhưng class access flags không private; không mở rộng một member private thực sự. |
| `HumanizedSellRuntime.java` | Boolean được đổi rõ ràng thành 0/1; giữ hai lần gọi getter theo thứ tự cũ. `LCMP` dùng `Long.compare`, giữ eager bitwise và tránh overflow của phép trừ long do decompile. |
| `SeaSettingsScreen.java` | Map có key `Field`; tách nhánh boxing `Integer`/`Double`, đúng bytecode gốc. Ternary decompile cũ sẽ box mọi giá trị thành Double. |
| `SellRhythmRuntime.java` | List slot có generic Integer. |
| `DisconnectRuntime.java`, `DisconnectSettings.java`, `SwordRecoveryRuntime.java` | Khôi phục generic player/string/text để vòng lặp đúng kiểu. |
| `AhScreenCloseFix.java`, `DeathRecoveryRuntime.java`, `HomePositionGuardRuntime.java`, `SlashSellRuntime.java` | Dùng `new Object[]{null}` cho Method.invoke để truyền một tham số null, khớp `anewarray/aastore` trong bytecode gốc. |
| `SlashSellSettings.java` | Khôi phục thứ tự construction options theo `<clinit>`; giữ construction list tạm nhưng chỉ gán final TOGGLE_OPTIONS một lần vào list 4 phần tử. Gốc không đọc list tạm giữa hai assignments. `snapshot()` vẫn chạy trước ghi `initialized=false`. |

Tổng 13 file Java được sửa so với baseline. Các getter
`isHumanizedSellV2Enabled()` và `isBulkSameItemTransferEnabled()` trả false
trong bytecode gốc và vẫn trả false trong source; không tự bật tính năng ẩn.
Các version string cũ, command strings và nội dung tài nguyên giữ nguyên.

Differential check dùng ASM 9.9 để đọc classfile mà không khởi tạo mod:

- Copy duy nhất `SeaSettingsScreen.parse` và lambda liên quan vào lớp không có
  superclass Minecraft, giữ record Entry, thay Config bằng các field cùng type,
  cung cấp draft map thủ công. 28 input kiểm tra Integer/Double, boolean/string/list,
  trim/deduplicate, giá trị biên, số thập phân, dấu phẩy, NaN/Infinity và invalid input.
  So sánh type/value và exception class/message giữa gốc và rebuild.
- Copy hai phương thức arithmetic vào lớp inert, thay nanoTime bằng clock field
  xác định và getter bulk bằng stub có đếm lần gọi. 300 trường hợp trên long min/max,
  deadline zero/negative, các boolean và burst values; output/latch/call counts khớp.
- Nạp riêng helper `AhScreenCloseFix` không có initializer/entrypoint; dùng mock client
  không có player, xác nhận fallback gọi `method_1507` đúng một lần với một null argument.

Các thay thế chỉ thuộc harness, không sửa production bytecode. Không thực thi
entrypoint, networking, webhook, command gửi server hoặc config thật.
Pure checks không chứng minh toàn bộ state machine hay GUI hoạt động trong game.

Annotation khác byte thô ở 7 mixin do targets chuyển dotted sang slash name và
Inject.at scalar chuyển thành array một phần tử. Chỉ chuẩn hóa hai biểu diễn đã
quan sát này khi so sánh; Mixin hỗ trợ chúng qua
[ClassInfo name normalization](https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/transformer/ClassInfo.java)
và [Annotations list handling](https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/util/Annotations.java).
Không thay target class, target method, injection point hoặc require.

## D. Artifact và mức sẵn sàng

**READY cho chỉnh sửa và build. Runtime/release chưa xác minh.**

- Runtime JAR: `build/libs/DonutUtility-CoreV2-restored-1.1.7.jar`.
- SHA-256 runtime: `970e7728f9f4915e4079e424fedcd530bd80108006238fd41f5042342a40c743`.
- Java source JAR: `build/libs/DonutUtility-CoreV2-restored-1.1.7-sources.jar`.
- Tất cả 146 class gốc còn tồn tại; thêm đúng hai lớp dữ liệu đã duyệt.
- Tất cả class major 65; không thấy duplicate entries hoặc invalid class headers.
- 39 resource giữ đủ; chỉ manifest khác, với build provenance của Loom thay
  Created-By của build gốc. Manifest gốc không có entrypoint đặc biệt/signature.
- `fabric.mod.json`, mixin JSON, assets, LICENSE và các patch notes khớp byte gốc.
- Mọi class byte thay đổi khi recompilation; không khẳng định bytecode hoặc hành vi
  toàn bộ mod tương đương chỉ từ inventory/descriptors.

Rủi ro cần chú ý khi phát triển: hai helper có initializer suy luận, tên Intermediary
khó đọc, reflection phụ thuộc field/method Minecraft và menu/chat riêng của server.
Phạm vi dependency trong metadata rộng hơn bộ build pins đã kiểm tra.

## E. Tiếp tục chỉnh sửa

1. Import Gradle bằng JDK 21, xem README để chọn file đúng tính năng.
2. Sửa source và chạy `build.ps1`; kiểm tra thêm logic mà thay đổi tác động.
3. Khi có nhu cầu xác minh trong game, dùng profile/config kiểm thử riêng và kiểm tra
   load, GUI, phím tắt, HUD, save/load trước khi thử automation với server.

Logs và bằng chứng nằm tại `D:/CoreV2/restoration/logs/`: decompile, Gradle version,
hai lần compile, build, jar-comparison JSON, raw/canonical annotation comparison,
differential output, danh sách source thay đổi và javap của các lớp có sửa quan trọng.
Harness ở `D:/CoreV2/restoration/tools/VerifyRestoration.java`.
Không chạy Minecraft trong phiên khôi phục này.
