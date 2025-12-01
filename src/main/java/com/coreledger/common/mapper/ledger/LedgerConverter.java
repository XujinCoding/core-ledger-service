package com.coreledger.common.mapper.ledger;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.ledger.LedgerItemDTO;
import com.coreledger.entity.Ledger;
import com.coreledger.entity.LedgerItem;
import com.coreledger.vo.LedgerListVO;
import com.coreledger.vo.LedgerVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 账单转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface LedgerConverter {

    /**
     * Entity -> VO（详情）
     *
     * @param entity 账单实体
     * @return 账单详情VO
     */
    @Mapping(target = "ledgerStatusDesc", source = "ledgerStatus.description")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "remainingAmount", expression = "java(calculateRemainingAmount(entity))")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "paymentRecords", ignore = true)
    LedgerVO toVO(Ledger entity);

    /**
     * Entity -> ListVO（列表）
     *
     * @param entity 账单实体
     * @return 账单列表VO
     */
    @Mapping(target = "ledgerStatusDesc", source = "ledgerStatus.description")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "remainingAmount", expression = "java(calculateRemainingAmount(entity))")
    LedgerListVO toListVO(Ledger entity);

    /**
     * Entity List -> ListVO List
     *
     * @param entities 账单实体列表
     * @return 账单列表VO列表
     */
    List<LedgerListVO> toListVOList(List<Ledger> entities);

    /**
     * DTO -> Entity（新增明细）
     *
     * @param dto 明细DTO
     * @return 明细实体
     */
    LedgerItem itemDtoToEntity(LedgerItemDTO dto);

    /**
     * 计算剩余欠款
     *
     * @param entity 账单实体
     * @return 剩余欠款金额
     */
    default java.math.BigDecimal calculateRemainingAmount(Ledger entity) {
        if (entity == null) {
            return java.math.BigDecimal.ZERO;
        }
        return entity.getTotalAmount()
                .subtract(entity.getPaidAmount())
                .subtract(entity.getDiscountAmount());
    }
}
