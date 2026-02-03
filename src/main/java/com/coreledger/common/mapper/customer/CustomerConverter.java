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
    @Mapping(target = "customerNo", source = "code")
    @Mapping(target = "addressPath", ignore = true)
    @Mapping(target = "remark", source = "memo")
    CustomerVO toVO(Customer entity);

    /**
     * 更新Entity
     *
     * @param dto 更新DTO
     * @param entity 实体
     */
    @Mapping(target = "memo", source = "remark")
    void updateEntity(CustomerUpdateDTO dto, @MappingTarget Customer entity);
}
