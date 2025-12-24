package com.coreledger.common.mapper.merchant;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.entity.Merchant;
import com.coreledger.vo.merchant.MerchantVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 商户Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface MerchantConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    @Mapping(target = "addressPath", ignore = true)
    MerchantVO toVO(Merchant entity);
}
