package com.coreledger.common.mapper.ledger;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.ledger.LedgerItemDTO;
import com.coreledger.entity.LedgerItem;
import com.coreledger.vo.LedgerItemVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 账单明细转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface LedgerItemConverter {

    /**
     * DTO -> Entity
     *
     * @param dto 明细DTO
     * @return 明细实体
     */
    LedgerItem toEntity(LedgerItemDTO dto);

    /**
     * Entity -> VO
     *
     * @param entity 明细实体
     * @return 明细VO
     */
    LedgerItemVO toVO(LedgerItem entity);

    /**
     * Entity List -> VO List
     *
     * @param entities 明细实体列表
     * @return 明细VO列表
     */
    List<LedgerItemVO> toVOList(List<LedgerItem> entities);
}
