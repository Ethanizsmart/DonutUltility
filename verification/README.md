# Kiểm chứng bản tích hợp

Từ thư mục dự án, build bằng JDK 21 trước. `build` tự chạy 54 checks của
`src/auctionChecks/java/dev/autoominous/AuctionSwordRulesChecks.java`.

40 checks Death Recovery/cầu nối bán bằng fixture inert (không Minecraft/mạng/cấu hình thật),
gồm các trường hợp Master OFF:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\VerifyRuntimePort.ps1 -JdkHome 'D:\Android Studio\jbr'
```

Script lấy ASM 9.9 từ cache Gradle của build và chỉ ghi scaffolding vào
`build/port-fixtures`. Source harness nằm trong `RuntimePortChecks.java`.

36 checks Master/F8 và 12 checks guard trong bytecode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\VerifyMasterF8.ps1 -JdkHome 'D:\Android Studio\jbr'
```

Harness dùng các class Master/settings/key callback đã build thực, với dependency
Minecraft/MaLiLib/runtime thay bằng fixture inert. Kiểm tra guard tĩnh không thay
thế cho việc thử GUI và automation trong game.

Đối chiếu bytecode/source seller với baseline trước tích hợp (baseline không kèm ZIP):

```powershell
.\verification\VerifySellerPreserved.ps1 -Project 'D:\CoreV2\DonutUtility-restored' -Baseline 'D:\CoreV2\death-recovery-port\baseline'
```

Các JSON kèm theo là bằng chứng của lần kiểm tra lúc bàn giao, không phải chứng nhận
cho các bản sửa sau đó. Xem `../DEATH_RECOVERY_PORT.md` để biết phạm vi và giới hạn.
