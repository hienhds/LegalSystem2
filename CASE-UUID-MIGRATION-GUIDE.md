# Hướng dẫn chuyển đổi Case ID từ Long sang UUID

## Hiện trạng
- Case entity đang dùng `Long id` với `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Bạn đang cố gắng sử dụng UUID `dd4f977a-ae57-45a1-aa38-fe608c066fc5`

## Nếu muốn chuyển sang UUID, cần thay đổi:

### 1. Case Entity
```java
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
@Column(name = "id", updatable = false, nullable = false)
private String id;  // hoặc UUID
```

### 2. Controller
```java
@GetMapping("/{id}")
public ApiResponse<CaseResponse> getCaseById(
        @PathVariable("id") String id,  // Thay Long → String
        HttpServletRequest httpRequest) {
```

### 3. Service & Repository
```java
public interface CaseRepository extends JpaRepository<Case, String> {  // Thay Long → String
```

### 4. DTOs
Thay đổi tất cả `Long id` → `String id` trong:
- CaseResponse
- CaseEvent
- CaseDocumentResponse
- CaseUpdateResponse

### 5. Database Migration
```sql
ALTER TABLE cases MODIFY COLUMN id VARCHAR(36) NOT NULL;
```

## Khuyến nghị
- **Giữ nguyên Long ID**: Đơn giản, hiệu quả, đủ dùng
- **Chuyển UUID**: Chỉ khi cần distributed system, không lộ số lượng records
