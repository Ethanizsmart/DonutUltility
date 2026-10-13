# Master Automation — F8

Bản cập nhật cục bộ trên source CoreV2 1.1.8. Release v1.1.8 đã đăng trước đó
được giữ làm baseline; tài liệu này mô tả phần Master thêm sau release đó.

## Sử dụng

- Bấm **F8** để bật/tắt chung các chức năng auto. F8 hoạt động cả khi đang mở GUI
  hoặc giữ phím khác; chỉ kích hoạt lúc nhấn, không bật/tắt lần nữa khi nhả phím.
- Mở GUI bằng phím cấu hình hiện tại (mặc định X+C). Nút **Master (F8): ON/OFF**
  nằm trên cùng và có mặt ở mọi tab. F8 cập nhật nhãn ngay nếu GUI đang mở.
- Master bắt đầu **OFF** mỗi lần khởi động game; trạng thái này không lưu vào config.
- Master ON cho phép các chức năng đã bật riêng chạy. Các chức năng đã OFF riêng
  vẫn OFF. Tắt/bật Master không sửa lựa chọn hoặc khoảng thời gian của từng chức năng.
  Các thay đổi cấu hình trong GUI vẫn được đồng bộ/lưu khi Master đang OFF.

## Phạm vi dừng

Master OFF chặn `/sell` (kể cả lượt ưu tiên sau hồi sinh), Hold RMB/Auto Use,
Death Recovery, Auto Buy Sword, Home Guard, Auto TPA, Auto Bounty, Auto Disconnect,
Auto Reconnect và nhánh tự relog khi GUI bán lỗi. Nhả input mod đang giữ, hủy
trạng thái giao dịch/recovery và các yêu cầu đang chờ; không xác nhận mua/bán.
Menu do automation đang sở hữu được đóng/hủy khi dừng. Không đóng GUI không do
automation sở hữu. Việc tắt Master không hoàn tác lệnh mà server đã nhận.

Giữ HUD/thống kê thụ động. Khi bật lại, runtime khởi đầu từ trạng thái mới;
không tiếp tục yêu cầu TPA, countdown reconnect hoặc recovery đã hủy.
Home anchor và cấu hình từng chức năng vẫn được giữ.

Thuật toán `/sell` CoreV2 giữ nguyên: **27 file nguồn và 34 class seller giống từng
byte** với baseline trước tích hợp. Master chặn tại các điểm điều phối/entrypoint
và reset bằng các API hiện có, không nhập thuật toán bán của release donor.

## Kiểm chứng

- Compile/build thành công trên JDK 21, Minecraft 1.21.11; `build.ps1` chạy được.
- 54 checks Auction/timer; 40 checks Death Recovery/cầu nối bán, gồm Master OFF.
- 36 checks dùng class Master/settings/key callback thực với fixture inert:
  F8/ANY/PRESS, đăng ký hotkey, cập nhật GUI, ON/OFF, nhả phím không toggle,
  reset các runtime phụ thuộc, giữ các lựa chọn ON/OFF riêng, xử lý menu sở hữu,
  idempotence và bật/tắt khi mất kết nối.
- 12 checks tĩnh bytecode xác nhận guard Master đứng trước lời gọi thao tác ở các
  entrypoint auto. Các checks này không xác nhận giao diện hoặc giao dịch thực.
- Runtime JAR: 156 → 157 class, thêm `MasterAutomation`; 21 class cũ đổi bytecode,
  không thiếu class/resource, không entry trùng hoặc header lỗi. Mixin config giữ
  nguyên; `fabric.mod.json` chỉ bổ sung mô tả F8, version vẫn 1.1.8.
- Kiểm tra đều offline với scaffolding thay Minecraft/MaLiLib/runtime dependencies;
  không chạy Minecraft, entrypoint mạng hoặc dùng file cấu hình game thật.

**Chưa thử trong Minecraft.** Chưa kiểm chứng hình ảnh GUI, phím F8 qua hệ thống
input thực hoặc hành vi hủy giao dịch trên server thật.

## Artifact và source

- Runtime: `../DonutUtility-CoreV2-1.1.8-MASTER-F8.jar`.
- Dự án đầy đủ: `../DonutUtility-CoreV2-1.1.8-MASTER-F8-source.zip`.
- SHA-256 runtime: `5feef5c630fa4174327a24fc67c8315b734a945e0755e342233aa3c6fe0fa963`.

Thay JAR Donut Utility cũ, chỉ giữ một bản trong `mods`; mở lại game rồi bấm F8
để cho phép các auto đã cấu hình chạy. ZIP là dự án để chỉnh sửa, không phải mod cài.
Build theo README; kiểm tra riêng bằng `verification/VerifyMasterF8.ps1` và
`verification/VerifyRuntimePort.ps1`. Log/baseline ở `../master-f8/`; JSON kiểm chứng
Master nằm trong `verification/master-*.json`.
