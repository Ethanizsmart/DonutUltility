# Kiểm chứng bản tích hợp

Từ thư mục dự án, build bằng JDK 21 trước. `build` tự chạy 54 checks của
`src/auctionChecks/java/dev/autoominous/AuctionSwordRulesChecks.java`.

35 checks bằng fixture inert (không Minecraft/mạng/cấu hình thật):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\verification\VerifyRuntimePort.ps1 -JdkHome 'D:\Android Studio\jbr'
```

Script lấy ASM 9.9 từ cache Gradle của build và chỉ ghi scaffolding vào
`build/port-fixtures`. Source harness nằm trong `RuntimePortChecks.java`.

Đối chiếu bytecode/source seller với baseline trước tích hợp (baseline không kèm ZIP):

```powershell
.\verification\VerifySellerPreserved.ps1 -Project 'D:\CoreV2\DonutUtility-restored' -Baseline 'D:\CoreV2\death-recovery-port\baseline'
```

Các JSON kèm theo là bằng chứng của lần kiểm tra lúc bàn giao, không phải chứng nhận
cho các bản sửa sau đó. Xem `../DEATH_RECOVERY_PORT.md` để biết phạm vi và giới hạn.
