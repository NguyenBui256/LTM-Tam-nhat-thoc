# Game Tấm nhặt thóc thi đấu đối kháng online

Hệ thống có một server và nhiều client. Server lưu toàn bộ thông tin và dữ liệu.  
Để chơi, người chơi phải login vào tài khoản của mình từ một máy client. Sau khi login thành công, giao diện hiện lên một danh sách người chơi đang online, mỗi người chơi có các thông tin:

- **Tên**  
- **Điểm bảng xếp hạng**  
- **Trạng thái** (đang bận nếu đang chơi với người khác, hoặc đang rỗi nếu chưa chơi với ai).

Muốn mời (thách đấu) ai thì người chơi click vào tên của đối thủ đó trong danh sách online.  
Khi bị thách đấu, người chơi có thể:

- **Chấp nhận (OK)**  
- **Từ chối (Reject)**

Khi chấp nhận, 2 người chơi sẽ vào phòng chơi với nhau, và server sẽ làm trọng tài.

---

## Giao diện chơi gồm:

- Danh sách các hạt (thóc, gạo, ngô…) được trộn lẫn ngẫu nhiên.  
- Khu vực để người chơi phân loại hạt (click chọn hạt sau đó bấm phím trên bàn phím để phân về đúng rổ tương ứng).  
- Các rổ đựng gạo, thóc, ngô có đánh số để người dùng biết phím cần bấm trên bàn phím, trên rổ cũng sẽ hiển thị số lượng hạt tương ứng đã nhặt được.  
- Đồng hồ đếm ngược thời gian còn lại.  
- Điểm số của mình và của đối thủ.  
- Nút **Thoát**.  

---

## Luật chơi

- Mỗi ván có **30 giây** để phân loại. Người chơi cần phân loại đúng càng nhiều hạt càng tốt.  
- Hạt phân loại đúng: **+1 điểm**  
- Hạt phân loại sai: **-1 điểm** (hoặc -0.5 điểm tùy cấu hình).  

Trong trận đấu:  

- Nếu một người chơi đã lấy được hạt thóc, sẽ có hoạt ảnh hạt thóc bay về rổ của người chơi đó, hạt đó sẽ không còn trên bàn chung.  
- Nếu 2 người cùng tranh nhau một hạt thóc, ai gửi được yêu cầu đến server trước và được xử lý thành công trước thì sẽ nhận được hạt thóc. Người còn lại sẽ hiện thông báo **Miss** hoặc **Mất**.  

Khi hết thời gian:  

- Server sẽ chấm điểm, tổng hợp kết quả và gửi về cho cả hai người chơi.  
- Sau mỗi ván, server sẽ tính điểm chênh lệch giữa 2 người:  
  - Người thắng được cộng **elo = ½ điểm hiệu số**  
  - Người thua bị trừ **elo = ½ điểm hiệu số**  

Giao diện hiển thị thông báo chơi tiếp cho cả 2 người chơi.  
- Nếu cả hai cùng đồng ý → tạo ván mới.  
- Nếu một trong hai từ chối → trận đấu kết thúc.  

Nếu muốn kết thúc trò chơi với đối thủ hiện tại, người chơi cũng có thể click vào nút **Thoát**, hệ thống sẽ thông báo với người còn lại.  

Kết quả các trận đấu được lưu vào server.  
Mỗi người chơi có thể vào xem bảng xếp hạng của toàn bộ hệ thống, theo tiêu chí:  

1. **Tổng số điểm** (giảm dần).  
2. **Tổng số trận thắng** (giảm dần).  

---

## Phân công công việc

### Module 1: Quản lý Server & Tài khoản người chơi *(Phạm Ngọc Long)*
- Xây dựng server, quản lý socket và kết nối nhiều client.  
- Cài đặt chức năng đăng ký, đăng nhập, đăng xuất.  
- Quản lý danh sách người chơi online và trạng thái bận/rỗi.  
- Quản lý thông tin người dùng.  

### Module 2: Xử lý Thách đấu & Phòng chơi *(Đỗ Lý Minh Anh)*
- Cài đặt chức năng gửi lời mời, chấp nhận/từ chối.  
- Tạo và quản lý phòng chơi khi có trận đấu.  
- Đồng bộ trạng thái giữa các client khi bắt đầu/kết thúc phòng.  
- Tái đấu.  

### Module 3: Phát triển Gameplay – Phân loại hạt & Tính điểm *(Bùi Thế Vĩnh Nguyên)*
- Sinh danh sách hạt ngẫu nhiên (theo seed chung cho cả 2 người).  
- Xử lý dữ liệu phân loại hạt từ client gửi lên.  
- Chấm điểm (đúng +1, sai -1 hoặc -0.5), gửi kết quả và cập nhật realtime.  
- Quản lý lưu kết quả trận, lịch sử thi đấu và bảng xếp hạng (theo tổng điểm, số trận thắng).  

### Module 4: Xây dựng Giao diện & Bảng xếp hạng *(Đỗ Hải Nam)*
- Thiết kế giao diện: danh sách hạt, khu vực phân loại, đồng hồ đếm ngược, điểm số.  
- Hiển thị điểm của bản thân và đối thủ theo thời gian thực.  
