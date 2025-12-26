# PHÂN TÍCH API APPOINTMENT - LEGAL SYSTEM

## Tổng quan
Phân tích dựa trên 7 use cases chính của module Appointment để xác định các API còn thiếu hoặc cần cải thiện.

---

## ✅ APIs ĐÃ CÓ (user-service)

### 1. Quản lý Appointments
| Endpoint | Method | Chức năng | Use Case |
|----------|--------|-----------|----------|
| `/api/appointments` | POST | Tạo lịch hẹn mới | UC1: Đặt lịch hẹn |
| `/api/appointments/my-appointments` | GET | Xem danh sách lịch hẹn của khách hàng | UC3: Xem lịch hẹn của tôi |
| `/api/appointments/lawyer-appointments` | GET | Xem danh sách lịch hẹn của luật sư | UC3, UC6: Xem yêu cầu đặt lịch |
| `/api/appointments/{id}` | GET | Xem chi tiết 1 lịch hẹn | UC3: Xem chi tiết |
| `/api/appointments/{id}/confirm` | POST | Luật sư chấp nhận lịch hẹn | UC7: Chấp nhận lịch hẹn |
| `/api/appointments/{id}/reject` | POST | Luật sư từ chối lịch hẹn | UC7: Từ chối lịch hẹn |
| `/api/appointments/{id}/cancel` | POST | Hủy lịch hẹn | UC4: Hủy lịch hẹn |
| `/api/appointments/{id}/complete` | POST | Hoàn thành lịch hẹn | - |
| `/api/appointments/{id}/rate` | POST | Đánh giá lịch hẹn | - |

---

## ❌ APIs THIẾU - CẦN BỔ SUNG

### 1. **Quản lý lịch làm việc của Luật sư (UC5: Manage Working Schedule)**

#### 1.1 LawyerAvailability CRUD APIs
```
❌ POST   /api/lawyer-schedule/availability
   Body: {
     "lawyerId": Long,
     "dayOfWeek": Integer (1-7),
     "startTime": "HH:mm:ss",
     "endTime": "HH:mm:ss",
     "isActive": Boolean
   }
   Response: LawyerAvailabilityResponse
   Mô tả: Luật sư tạo khung giờ làm việc hàng tuần
```

```
❌ GET    /api/lawyer-schedule/availability/lawyer/{lawyerId}
   Response: List<LawyerAvailabilityResponse>
   Mô tả: Lấy tất cả khung giờ làm việc của luật sư
```

```
❌ PUT    /api/lawyer-schedule/availability/{availabilityId}
   Body: {
     "dayOfWeek": Integer,
     "startTime": "HH:mm:ss",
     "endTime": "HH:mm:ss",
     "isActive": Boolean
   }
   Response: LawyerAvailabilityResponse
   Mô tả: Cập nhật khung giờ làm việc
```

```
❌ DELETE /api/lawyer-schedule/availability/{availabilityId}
   Response: Success message
   Mô tả: Xóa khung giờ làm việc
```

**⚠️ CHÚ Ý:** Cần validation:
- ✅ Kiểm tra trùng lặp khung giờ (overlap detection)
- ✅ Không cho xóa/sửa khung giờ đã có lịch hẹn CONFIRMED
- ✅ Không cho tạo khung giờ trong quá khứ
- ✅ startTime < endTime

---

### 2. **Kiểm tra lịch trống (UC2: Check Lawyer Availability)**

#### 2.1 API kiểm tra khung giờ trống của luật sư
```
❌ GET    /api/lawyer-schedule/available-slots/lawyer/{lawyerId}
   Params: 
     - date: LocalDate (yyyy-MM-dd)
     - durationMinutes: Integer (optional, default: 60)
   Response: {
     "date": "2025-12-26",
     "lawyerId": 1,
     "lawyerName": "Nguyễn Văn A",
     "availableSlots": [
       {
         "startTime": "09:00:00",
         "endTime": "10:00:00",
         "durationMinutes": 60
       },
       {
         "startTime": "14:00:00",
         "endTime": "15:00:00",
         "durationMinutes": 60
       }
     ],
     "bookedSlots": [
       {
         "startTime": "10:00:00",
         "endTime": "11:00:00",
         "appointmentId": 123
       }
     ]
   }
   Mô tả: 
   - Lấy lịch làm việc của luật sư theo ngày trong tuần
   - Loại bỏ các khung giờ đã có appointment (PENDING, CONFIRMED)
   - Chia thành các time slots theo duration
   - Xử lý ngoại lệ:
     * Luật sư chưa thiết lập lịch làm việc
     * Không còn khung giờ trống
```

#### 2.2 API kiểm tra nhiều ngày
```
❌ GET    /api/lawyer-schedule/available-slots/lawyer/{lawyerId}/range
   Params:
     - startDate: LocalDate
     - endDate: LocalDate (max 14 days from startDate)
     - durationMinutes: Integer
   Response: {
     "lawyerId": 1,
     "lawyerName": "Nguyễn Văn A",
     "dateRange": {
       "startDate": "2025-12-26",
       "endDate": "2026-01-09"
     },
     "availability": [
       {
         "date": "2025-12-26",
         "totalSlots": 8,
         "availableSlots": 5,
         "slots": [...]
       }
     ]
   }
   Mô tả: Xem lịch trống trong khoảng thời gian (hữu ích cho calendar view)
```

---

### 3. **Validation và Business Logic cần bổ sung**

#### 3.1 Hủy lịch hẹn (UC4: Cancel Appointment)
```java
// Cần thêm vào AppointmentService.cancelAppointment()

⚠️ THIẾU: Validation quy tắc "không được hủy trước 2 tiếng"

public AppointmentResponse cancelAppointment(...) {
    // ... existing code ...
    
    // ❌ THIẾU: Check 2-hour cancellation policy
    LocalDateTime appointmentDateTime = LocalDateTime.of(
        appointment.getAppointmentDate(), 
        appointment.getAppointmentTime()
    );
    LocalDateTime now = LocalDateTime.now();
    long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointmentDateTime);
    
    if (hoursUntilAppointment < 2 && hoursUntilAppointment >= 0) {
        throw new AppException(
            ErrorType.BUSINESS_RULE_VIOLATION,
            "Không thể tự hủy lịch hẹn vì đã quá thời hạn quy định. " +
            "Vui lòng liên hệ trực tiếp để giải quyết."
        );
    }
    
    // ... rest of code ...
}
```

#### 3.2 Tạo lịch hẹn (UC1: Book Appointment)
```java
// Cần cải thiện AppointmentService.createAppointment()

⚠️ CẦN CẢI THIỆN: 
1. Check xem luật sư có thiết lập lịch làm việc cho ngày đó không
2. Check xem thời gian đặt có nằm trong khung giờ làm việc không
3. Tích hợp với notification service để gửi thông báo cho luật sư

public AppointmentResponse createAppointment(...) {
    // ... existing validations ...
    
    // ❌ THIẾU: Validate appointment time is within working hours
    boolean isWithinWorkingHours = lawyerAvailabilityRepository
        .existsByLawyerAndDayAndTimeRange(
            request.getLawyerId(),
            request.getAppointmentDate().getDayOfWeek().getValue(),
            request.getAppointmentTime()
        );
    
    if (!isWithinWorkingHours) {
        throw new AppException(
            ErrorType.BUSINESS_RULE_VIOLATION,
            "Thời gian đặt lịch không nằm trong khung giờ làm việc của luật sư"
        );
    }
    
    // ❌ THIẾU: Send notification to lawyer
    // notificationService.notifyNewAppointmentRequest(appointment);
    
    // ... rest of code ...
}
```

---

## 📋 Repository Methods CẦN BỔ SUNG

### LawyerAvailabilityRepository (CHƯA TỒN TẠI)
```java
// File: user-service/src/main/java/com/example/userservice/appointment/repository/LawyerAvailabilityRepository.java

@Repository
public interface LawyerAvailabilityRepository extends JpaRepository<LawyerAvailability, Long> {
    
    // Lấy tất cả lịch làm việc của luật sư
    List<LawyerAvailability> findByLawyer_LawyerIdAndIsActiveTrue(Long lawyerId);
    
    // Lấy lịch làm việc theo ngày trong tuần
    List<LawyerAvailability> findByLawyer_LawyerIdAndDayOfWeekAndIsActiveTrue(
        Long lawyerId, 
        Integer dayOfWeek
    );
    
    // Kiểm tra trùng lặp khung giờ
    @Query("SELECT COUNT(la) > 0 FROM LawyerAvailability la " +
           "WHERE la.lawyer.lawyerId = :lawyerId " +
           "AND la.dayOfWeek = :dayOfWeek " +
           "AND la.isActive = true " +
           "AND la.availabilityId != :excludeId " +
           "AND ((la.startTime <= :endTime AND la.endTime >= :startTime))")
    boolean existsOverlappingAvailability(
        @Param("lawyerId") Long lawyerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );
    
    // Kiểm tra xem thời gian có nằm trong khung giờ làm việc không
    @Query("SELECT COUNT(la) > 0 FROM LawyerAvailability la " +
           "WHERE la.lawyer.lawyerId = :lawyerId " +
           "AND la.dayOfWeek = :dayOfWeek " +
           "AND la.isActive = true " +
           "AND la.startTime <= :time AND la.endTime >= :time")
    boolean existsByLawyerAndDayAndTimeRange(
        @Param("lawyerId") Long lawyerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("time") LocalTime time
    );
}
```

---

## 📦 DTOs CẦN BỔ SUNG

### LawyerAvailabilityRequest.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityRequest {
    
    @NotNull(message = "Lawyer ID is required")
    private Long lawyerId;
    
    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Builder.Default
    private Boolean isActive = true;
}
```

### LawyerAvailabilityResponse.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityResponse {
    private Long availabilityId;
    private Long lawyerId;
    private String lawyerName;
    private Integer dayOfWeek;
    private String dayOfWeekName; // "Thứ 2", "Thứ 3", ...
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### AvailableSlotResponse.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotResponse {
    private LocalDate date;
    private Long lawyerId;
    private String lawyerName;
    private List<TimeSlot> availableSlots;
    private List<BookedSlot> bookedSlots;
    private String message; // For error messages
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer durationMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookedSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private Long appointmentId;
        private AppointmentStatus status;
    }
}
```

---

## 🔧 Services CẦN TẠO

### LawyerScheduleService.java
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LawyerScheduleService {
    
    private final LawyerAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final LawyerRepository lawyerRepository;
    
    // CRUD operations cho LawyerAvailability
    public LawyerAvailabilityResponse createAvailability(LawyerAvailabilityRequest request);
    public LawyerAvailabilityResponse updateAvailability(Long id, LawyerAvailabilityRequest request);
    public void deleteAvailability(Long id);
    public List<LawyerAvailabilityResponse> getLawyerAvailabilities(Long lawyerId);
    
    // Business logic cho available slots
    public AvailableSlotResponse getAvailableSlots(Long lawyerId, LocalDate date, Integer duration);
    public Map<LocalDate, AvailableSlotResponse> getAvailableSlotsRange(
        Long lawyerId, 
        LocalDate startDate, 
        LocalDate endDate, 
        Integer duration
    );
    
    // Validation methods
    private void validateTimeRange(LocalTime startTime, LocalTime endTime);
    private void validateNoOverlap(Long lawyerId, Integer dayOfWeek, 
                                   LocalTime startTime, LocalTime endTime, Long excludeId);
    private void validateNoExistingAppointments(Long availabilityId);
}
```

---

## 🎯 Controller CẦN TẠO

### LawyerScheduleController.java
```java
@RestController
@RequestMapping("/api/lawyer-schedule")
@RequiredArgsConstructor
@Slf4j
public class LawyerScheduleController {
    
    private final LawyerScheduleService scheduleService;
    
    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<LawyerAvailabilityResponse>> createAvailability(...);
    
    @GetMapping("/availability/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> getLawyerAvailabilities(...);
    
    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<LawyerAvailabilityResponse>> updateAvailability(...);
    
    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(...);
    
    @GetMapping("/available-slots/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<AvailableSlotResponse>> getAvailableSlots(...);
    
    @GetMapping("/available-slots/lawyer/{lawyerId}/range")
    public ResponseEntity<ApiResponse<Map<LocalDate, AvailableSlotResponse>>> getAvailableSlotsRange(...);
}
```

---

## 📊 TỔNG KẾT

### APIs cần implement
✅ **9 APIs cơ bản đã có**
❌ **6 APIs còn thiếu cần bổ sung:**
1. POST `/api/lawyer-schedule/availability` - Tạo lịch làm việc
2. GET `/api/lawyer-schedule/availability/lawyer/{lawyerId}` - Xem lịch làm việc
3. PUT `/api/lawyer-schedule/availability/{id}` - Cập nhật lịch làm việc
4. DELETE `/api/lawyer-schedule/availability/{id}` - Xóa lịch làm việc
5. GET `/api/lawyer-schedule/available-slots/lawyer/{lawyerId}` - **QUAN TRỌNG** - Kiểm tra lịch trống
6. GET `/api/lawyer-schedule/available-slots/lawyer/{lawyerId}/range` - Kiểm tra lịch trống nhiều ngày

### Components cần tạo
❌ **1 Repository:** LawyerAvailabilityRepository
❌ **1 Service:** LawyerScheduleService  
❌ **1 Controller:** LawyerScheduleController
❌ **3 DTOs:** LawyerAvailabilityRequest, LawyerAvailabilityResponse, AvailableSlotResponse

### Validation/Business Logic cần bổ sung
⚠️ **Trong AppointmentService:**
- Validation "không được hủy trước 2 tiếng"
- Check thời gian đặt lịch có nằm trong working hours không
- Gửi notification khi tạo/hủy/xác nhận lịch hẹn

⚠️ **Trong LawyerScheduleService:**
- Kiểm tra trùng lặp khung giờ
- Không cho xóa khung giờ có lịch hẹn
- Không cho tạo lịch trong quá khứ

---

## 🚀 ƯU TIÊN TRIỂN KHAI

### Phase 1: CRITICAL (Cần ngay)
1. ✅ Tạo LawyerAvailabilityRepository
2. ✅ Tạo LawyerScheduleService với CRUD operations
3. ✅ Tạo LawyerScheduleController
4. ✅ API kiểm tra lịch trống: GET `/available-slots/lawyer/{lawyerId}`

### Phase 2: IMPORTANT (Quan trọng)
5. ⚠️ Bổ sung validation 2-hour cancellation policy
6. ⚠️ Validation thời gian đặt lịch nằm trong working hours
7. ⚠️ Validation overlap detection cho availability

### Phase 3: NICE TO HAVE
8. 📱 Tích hợp notification service
9. 📊 API xem lịch trống nhiều ngày (range)
10. 🔍 Advanced filtering và search

---

## 📝 GHI CHÚ
- Entity LawyerAvailability đã tồn tại ✅
- Entity Appointment đã tồn tại ✅
- AppointmentRepository đã có đầy đủ queries ✅
- **THIẾU hoàn toàn:** LawyerAvailability management APIs
- **THIẾU quan trọng:** API check available slots (UC2)
- **CẦN CẢI THIỆN:** Business logic validation
