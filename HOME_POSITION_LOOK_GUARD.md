# Home 1 Position + Look Guard

Bản build 1.1.8 này bổ sung kiểm tra **XYZ + hướng nhìn yaw/pitch** so với Home 1
đã lưu, theo yêu cầu. Lệch vị trí, khác dimension hoặc lệch một trong hai góc
sẽ kích hoạt lệnh ở mục `Home 1 CMD` (mặc định `/home 1`).

## Thiết lập trong game

1. Dùng `/home 1`, đứng đúng vị trí cần giữ và hướng nhân vật về phía mong muốn.
2. Mở tab **Death Recovery**, bấm **Save Home 1 Here (XYZ + Look)**. Nút này lưu
   vị trí/góc hiện tại, không gửi lệnh, và dùng được khi Master OFF.
3. Bật **Home 1 Position + Look Guard**, đóng cửa sổ cấu hình và bật **Master F8**.

Mod không đọc được dữ liệu Home phía server; nút Save Here coi vị trí bạn chọn
là Home 1. Nếu muốn kiểm chứng qua teleport, có thể đứng xa Home rồi bấm
**Capture Home 1**, đóng GUI và để lệnh Home đưa về vị trí ổn định. Capture
cần Master ON và một thay đổi vị trí tối thiểu 0.5 block hoặc thay đổi dimension.
Khi đã đứng ngay tại Home, dùng Save Here để tránh Capture không nhận được teleport.

“Tọa độ trỏ chuột” trong chức năng này là góc nhìn của nhân vật: yaw trái/phải,
pitch lên/xuống, theo lựa chọn của người dùng.

## Ngưỡng và luồng hoạt động

- Mặc định **0.3 block** cho khoảng cách ngang XZ và độ lệch Y. Ngưỡng Y trước
  đây là 1 block; nay dùng chung giá trị chỉnh được ở Home Position Tolerance.
- Mặc định **2 độ** cho từng góc yaw/pitch. Yaw được so sánh theo vòng 360 độ;
  hướng +179 độ và -179 độ chỉ lệch 2 độ. Chỉnh ở Home Look Tolerance.
- Lệch liên tục **400–600 ms** mới gửi lệnh để bỏ qua dao động ngắn.
- Chờ cả vị trí và hướng nhìn đúng rồi ổn định **650 ms** trước khi nhả quyền
  automation; cooldown 1 giây sau khi hoàn tất.
- Tối đa hai lần gửi trong một lượt sửa: lần đầu, một lần thử lại sau 10 giây;
  thêm 10 giây vẫn sai thì dừng và khóa lượt sửa lỗi đó. Đứng lại đúng pose,
  lưu lại anchor hoặc tắt/bật Home Guard để mở lượt mới.
- Chờ GUI đóng, lượt `/sell`, mua kiếm và Bounty hoàn tất; TPA có thêm 2 giây
  chờ sau khi hết trạng thái bận. Death Recovery/Vex được ưu tiên và hủy lượt Home.
  Lần thử lại cũng nhường GUI/TPA/giao dịch và command backoff.
- Tắt Home Guard hủy lượt RETURN; F8 OFF hủy lượt đang chạy và ngăn gửi lệnh.
  Các lựa chọn riêng của auto được giữ như bản Master F8 trước đó.

Mod gửi lệnh Home, không tự ép camera về góc đã lưu. Server cần trả cả hướng
nhìn về đúng pose để lượt sửa hoàn tất. Nếu server chỉ đưa về XYZ mà không
đặt lại góc nhìn, lượt sửa sẽ chạm giới hạn hai lần gửi và dừng. Có thể cần
lưu Home phía server lại với hướng nhìn mong muốn.

## Lưu cấu hình và bảo toàn

Anchor: `config/autoominous-home1-guard.properties`, thêm `yaw`, `pitch`,
`lookSet`, `captureMode`; giữ XYZ/world và marker capture cũ. Anchor verified cũ
vẫn kiểm tra vị trí nhưng cần lưu/capture lại để có góc nhìn. Anchor cũ chưa
được xác minh hoặc chứa tọa độ không hữu hạn không được bật để tự điều hướng.

Hai ngưỡng lưu trong `config/autoominous-death-recovery.properties`.
Giữ khóa `home1PositionGuard` cũ, mặc định OFF; không bật thêm auto khi cập nhật.
Không thay luồng `/sell`, policy bán, item dùng tự động hay HUD.

Đây là bổ sung cho bản Master F8. Không sửa riêng vấn đề Master reset có
thể đóng GUI Confirm Bounty thủ công đã được nêu trong lần đánh giá trước.

## Kiểm chứng

- Build thành công: Java 21, Minecraft 1.21.11, namespace Intermediary.
- 120 checks hình học góc/vị trí; 66 checks runtime Home với bytecode thực.
  Runtime fixtures thay Minecraft, các runtime phụ thuộc, thời gian và đường
  dẫn anchor bằng đối tượng inert/file trong `build/home-guard-fixtures`.
- Các checks hồi quy: 54 Auction/timer, 40 Death Recovery/bridge, 36 Master F8,
  12 guard bytecode ở entrypoint automation.
- Luồng bán: **27 source và 34 class giữ nguyên hash** so với baseline CoreV2
  trước khi nhập Death Recovery; bản donor không thay seller.
- Runtime JAR: 157 → 158 class; thêm HomeAnchorRules. Năm class đổi bytecode
  thuộc DeathRecoverySettings, HomePositionGuardRuntime, GUI và hai enum lồng
  được biên dịch lại. Không thiếu class/resource, không entry trùng/header lỗi.
  Chỉ resource `fabric.mod.json` đổi mô tả; version, entrypoint, mixin giữ nguyên.

**Chưa thử trong Minecraft/server thật**, chưa xác minh GUI hiển thị thực tế
hoặc server trả hướng nhìn sau lệnh Home. Kiểm tra offline không chạy entrypoint
mạng, không dùng cấu hình game thật.

```powershell
.\build.ps1
.\verification\VerifyHomeGuard.ps1
.\verification\VerifyRuntimePort.ps1
.\verification\VerifyMasterF8.ps1
.\verification\VerifySellerPreserved.ps1
```

## Artifact

- Mod: `../DonutUtility-CoreV2-1.1.8-HOME-LOOK-GUARD.jar`.
- Dự án Gradle đầy đủ: `../DonutUtility-CoreV2-1.1.8-HOME-LOOK-GUARD-source.zip`.
- Runtime SHA-256: `dd98738fc15f3a2dfd815baa57230a4464e6887fb0d031bd777e39dad0a57802`.
- Log và baseline: `../home-look-guard/`.

Thay JAR Donut Utility cũ, chỉ giữ một bản trong `mods`. ZIP/source JAR dùng để
chỉnh sửa, không cho vào `mods`. Bản này đã cập nhật lên
[release GitHub v1.1.8](https://github.com/Ethanizsmart/DonutUltility/releases/tag/v1.1.8):
`donututility-1.1.8.jar`, `donututility-1.1.8-sources.jar` và `donututility-1.1.8-source.zip`.
