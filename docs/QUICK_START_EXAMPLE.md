# 快速开始示例

## 完整的 CRUD 示例

本文档展示如何使用统一异常处理和响应格式实现一个完整的 CRUD 功能。

---

## 1. Entity 实体类

```java
@Data
@Entity
@Table(name = "customer")
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 性别 */
    @Column(name = "gender", nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender = Gender.UNKNOWN;
}
```

---

## 2. Repository 层

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByPhone(String phone);
    
    Page<Customer> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);
}
```

---

## 3. DTO 层

```java
@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String phone;
    private Gender gender;
}

@Data
public class CreateCustomerDTO {
    
    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 50, message = "客户姓名长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotNull(message = "性别不能为空")
    private Gender gender;
}
```

---

## 4. Service 层

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerConverter customerConverter;

    /**
     * 创建客户
     */
    @Transactional
    public CustomerDTO createCustomer(CreateCustomerDTO dto) {
        log.info("创建客户: name={}, phone={}", dto.getName(), dto.getPhone());
        
        // 1. 校验手机号是否已存在
        customerRepository.findByPhone(dto.getPhone()).ifPresent(customer -> {
            throw new BusinessException(ErrorCode.CUSTOMER_PHONE_EXISTS);
        });
        
        // 2. 转换并保存
        Customer customer = customerConverter.toEntity(dto);
        customer = customerRepository.save(customer);
        
        log.info("客户创建成功: id={}", customer.getId());
        return customerConverter.toDTO(customer);
    }

    /**
     * 查询客户详情
     */
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerConverter.toDTO(customer);
    }

    /**
     * 查询客户列表
     */
    public Page<CustomerDTO> listCustomers(String keyword, Pageable pageable) {
        Page<Customer> customerPage = customerRepository
            .findByNameContainingAndStatus(keyword, 1, pageable);
        return customerPage.map(customerConverter::toDTO);
    }

    /**
     * 更新客户
     */
    @Transactional
    public CustomerDTO updateCustomer(Long id, CreateCustomerDTO dto) {
        log.info("更新客户: id={}, name={}", id, dto.getName());
        
        // 1. 查询客户
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
        
        // 2. 校验手机号是否被其他客户使用
        if (!customer.getPhone().equals(dto.getPhone())) {
            customerRepository.findByPhone(dto.getPhone()).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(id)) {
                    throw new BusinessException(ErrorCode.CUSTOMER_PHONE_EXISTS);
                }
            });
        }
        
        // 3. 更新字段
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        customer.setGender(dto.getGender());
        
        customer = customerRepository.save(customer);
        log.info("客户更新成功: id={}", customer.getId());
        return customerConverter.toDTO(customer);
    }

    /**
     * 删除客户（软删除）
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("删除客户: id={}", id);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
        
        customer.setStatus(0);
        customerRepository.save(customer);
        
        log.info("客户删除成功: id={}", id);
    }
}
```

---

## 5. Controller 层

```java
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户的增删改查操作")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     */
    @PostMapping
    @Operation(summary = "创建客户")
    public Result<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerDTO dto) {
        CustomerDTO customerDTO = customerService.createCustomer(dto);
        return Result.success("创建成功", customerDTO);
    }

    /**
     * 查询客户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询客户详情")
    public Result<CustomerDTO> getCustomerById(
            @Parameter(description = "客户ID") @PathVariable Long id) {
        CustomerDTO customerDTO = customerService.getCustomerById(id);
        return Result.success(customerDTO);
    }

    /**
     * 查询客户列表
     */
    @GetMapping
    @Operation(summary = "查询客户列表")
    public Result<Page<CustomerDTO>> listCustomers(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<CustomerDTO> page = customerService.listCustomers(keyword, pageable);
        return Result.success(page);
    }

    /**
     * 更新客户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public Result<CustomerDTO> updateCustomer(
            @Parameter(description = "客户ID") @PathVariable Long id,
            @Valid @RequestBody CreateCustomerDTO dto) {
        CustomerDTO customerDTO = customerService.updateCustomer(id, dto);
        return Result.success("更新成功", customerDTO);
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public Result<Void> deleteCustomer(
            @Parameter(description = "客户ID") @PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success("删除成功");
    }
}
```

---

## 6. 响应示例

### 6.1 创建成功

**请求**:
```http
POST /api/customers
Content-Type: application/json

{
  "name": "张三",
  "phone": "13800138000",
  "gender": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "gender": 1
  },
  "timestamp": 1700000000000
}
```

### 6.2 参数校验失败

**请求**:
```http
POST /api/customers
Content-Type: application/json

{
  "name": "",
  "phone": "123"
}
```

**响应**:
```json
{
  "code": 400,
  "message": "客户姓名不能为空; 手机号格式不正确; 性别不能为空",
  "timestamp": 1700000000000
}
```

### 6.3 手机号已存在

**响应**:
```json
{
  "code": 1002,
  "message": "手机号已被注册",
  "timestamp": 1700000000000
}
```

### 6.4 客户不存在

**请求**:
```http
GET /api/customers/999
```

**响应**:
```json
{
  "code": 1001,
  "message": "客户不存在",
  "timestamp": 1700000000000
}
```

### 6.5 乐观锁冲突

**响应**:
```json
{
  "code": 9001,
  "message": "数据已被其他用户修改，请刷新后重试",
  "timestamp": 1700000000000
}
```

---

## 7. 关键要点总结

### 7.1 Service 层
- ✅ 使用 `orElseThrow()` 处理查询不存在的情况
- ✅ 抛出具体的业务异常，不要返回 null
- ✅ 记录关键操作日志
- ✅ 使用 `@Transactional` 管理事务

### 7.2 Controller 层
- ✅ 使用 `@Valid` 进行参数校验
- ✅ 不要捕获异常，让全局异常处理器处理
- ✅ 统一返回 `Result<T>` 格式
- ✅ 添加 Swagger 注解

### 7.3 异常处理
- ✅ 业务异常抛出 `BusinessException`
- ✅ 资源不存在抛出 `NotFoundException`
- ✅ 权限问题抛出 `ForbiddenException`
- ✅ 使用错误码枚举保持统一

---

**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
