package com.coreledger.converter;

import com.coreledger.dto.customer.CreateCustomerDTO;
import com.coreledger.dto.customer.UpdateCustomerDTO;
import com.coreledger.entity.Customer;
import com.coreledger.enums.Gender;
import com.coreledger.vo.customer.CustomerDetailVO;
import com.coreledger.vo.customer.CustomerListVO;
import org.mapstruct.*;

import java.util.List;

/**
 * 客户转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerConverter {

    /**
     * CreateDTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customerType", ignore = true)
    @Mapping(target = "gender", expression = "java(mapGender(dto.getGender()))")
    Customer toEntity(CreateCustomerDTO dto);

    /**
     * Entity → ListVO
     */
    @Mapping(target = "gender", expression = "java(mapGenderToInt(entity.getGender()))")
    @Mapping(target = "genderDesc", expression = "java(entity.getGender().getDescription())")
    @Mapping(target = "customerType", expression = "java(entity.getCustomerType().getValue())")
    @Mapping(target = "customerTypeDesc", expression = "java(entity.getCustomerType().getDescription())")
    @Mapping(target = "fullAddress", ignore = true)  // 需要通过地址服务查询
    @Mapping(target = "debtAmount", ignore = true)   // 需要通过MyBatis查询
    @Mapping(target = "creditAmount", ignore = true)
    @Mapping(target = "activeLedgerCount", ignore = true)
    CustomerListVO toListVO(Customer entity);

    /**
     * Entity → DetailVO
     */
    @Mapping(target = "gender", expression = "java(mapGenderToInt(entity.getGender()))")
    @Mapping(target = "genderDesc", expression = "java(entity.getGender().getDescription())")
    @Mapping(target = "customerType", expression = "java(entity.getCustomerType().getValue())")
    @Mapping(target = "customerTypeDesc", expression = "java(entity.getCustomerType().getDescription())")
    @Mapping(target = "fullAddress", ignore = true)       // 需要通过地址服务查询
    @Mapping(target = "ledgerSummary", ignore = true)     // 需要通过MyBatis查询
    CustomerDetailVO toDetailVO(Customer entity);

    /**
     * 批量转换 Entity → ListVO
     */
    List<CustomerListVO> toListVO(List<Customer> entities);

    /**
     * UpdateDTO → Entity (更新实体)
     * 只更新非null字段
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customerType", ignore = true)
    @Mapping(target = "gender", expression = "java(dto.getGender() != null ? mapGender(dto.getGender()) : entity.getGender())")
    void updateEntity(UpdateCustomerDTO dto, @MappingTarget Customer entity);

    /**
     * Integer → Gender 枚举映射
     */
    default Gender mapGender(Integer value) {
        if (value == null) {
            return Gender.UNKNOWN;
        }
        return Gender.fromValue(value);
    }

    /**
     * Gender → Integer 枚举映射
     */
    default Integer mapGenderToInt(Gender gender) {
        if (gender == null) {
            return 0;
        }
        return gender.getValue();
    }
}
