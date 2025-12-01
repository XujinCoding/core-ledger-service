package com.coreledger.common.mapper.ledger;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.entity.PaymentRecord;
import com.coreledger.vo.PaymentRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 支付记录转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface PaymentRecordConverter {

    /**
     * Entity -> VO
     *
     * @param entity 支付记录实体
     * @return 支付记录VO
     */
    @Mapping(target = "paymentMethodDesc", source = "paymentMethod.description")
    PaymentRecordVO toVO(PaymentRecord entity);

    /**
     * Entity List -> VO List
     *
     * @param entities 支付记录实体列表
     * @return 支付记录VO列表
     */
    List<PaymentRecordVO> toVOList(List<PaymentRecord> entities);
}
