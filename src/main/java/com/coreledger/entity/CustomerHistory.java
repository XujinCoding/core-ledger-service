package com.coreledger.entity;

import com.coreledger.config.converter.CustomerTypeConverter;
import com.coreledger.config.converter.GenderConverter;
import com.coreledger.config.converter.OperationTypeConverter;
import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import com.coreledger.enums.OperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 客户历史实体（快照模式）
 *
 * <p>保存客户表的完整记录快照</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer_history")
public class CustomerHistory {

    /** 历史记录ID（使用单独的主键） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    /** 客户ID */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 别名/昵称 */
    @Column(name = "alias", length = 50)
    private String alias;

    /** 性别 */
    @Column(name = "gender", nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender;

    /** 年龄 */
    @Column(name = "age")
    private Integer age;

    /** 关联地址ID */
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    /** 详细地址 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    /** 备注 */
    @Column(name = "memo", length = 255)
    private String memo;

    /** 客户类型 */
    @Column(name = "customer_type", nullable = false)
    @Convert(converter = CustomerTypeConverter.class)
    private CustomerType customerType;

    /** 状态 */
    @Column(name = "status", nullable = false)
    private Integer status;

    /** 创建时间 */
    @Column(name = "create_instant", nullable = false, updatable = false)
    private LocalDateTime createInstant;

    /** 修改时间 */
    @Column(name = "modify_instant", nullable = false)
    private LocalDateTime modifyInstant;

    /** 乐观锁版本号 */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /** 操作类型 */
    @Column(name = "operation_type", nullable = false)
    @Convert(converter = OperationTypeConverter.class)
    private OperationType operationType;

    /** 操作时间 */
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    /** 操作人ID */
    @Column(name = "operator_id")
    private Long operatorId;

    /** 操作人姓名 */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /**
     * 从 Customer 实体创建历史快照
     *
     * @param customer      客户实体
     * @param operationType 操作类型
     * @return 客户历史快照
     */
    public static CustomerHistory fromCustomer(Customer customer, OperationType operationType) {
        CustomerHistory history = new CustomerHistory();
        history.setCustomerId(customer.getId());
        history.setName(customer.getName());
        history.setPhone(customer.getPhone());
        history.setAlias(customer.getAlias());
        history.setGender(customer.getGender());
        history.setAge(customer.getAge());
        history.setAddressId(customer.getAddressId());
        history.setAddressDetail(customer.getAddressDetail());
        history.setCustomerType(customer.getCustomerType());
        history.setStatus(1); // 默认状态为启用
        history.setOperationType(operationType);
        history.setOperationTime(LocalDateTime.now());
        // TODO: 从上下文获取操作人信息
        // history.setOperatorId(currentUserId);
        // history.setOperatorName(currentUserName);
        return history;
    }
}
