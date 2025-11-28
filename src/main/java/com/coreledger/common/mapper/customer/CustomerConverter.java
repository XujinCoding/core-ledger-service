package com.coreledger.common.mapper.customer;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.customer.CustomerCreateDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.entity.Customer;
import com.coreledger.vo.customer.CustomerVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 客户Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface CustomerConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    @Mapping(target = "genderDesc", source = "gender.description")
    @Mapping(target = "customerTypeDesc", source = "customerType.description")
    @Mapping(target = "addressPath", ignore = true)
    CustomerVO toVO(Customer entity);

    /**
     * CreateDTO转Entity
     *
     * @param dto 创建DTO
     * @return 实体
     */
    Customer toEntity(CustomerCreateDTO dto);

    /**
     * 更新Entity
     *
     * @param dto 更新DTO
     * @param entity 实体
     */
    void updateEntity(CustomerUpdateDTO dto, @MappingTarget Customer entity);
}
