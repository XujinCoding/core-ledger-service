package com.coreledger.mapper;

import com.coreledger.dto.ledger.LedgerSearchDTO;
import com.coreledger.vo.LedgerListStatsVO;
import com.coreledger.vo.LedgerListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单Mapper接口（MyBatis）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper
public interface LedgerMapper {

    /**
     * 搜索账单列表（连表查询，支持客户姓名和电话模糊查询）
     *
     * @param dto 搜索条件
     * @return 账单列表
     */
    List<LedgerListVO> searchLedgers(@Param("dto") LedgerSearchDTO dto);

    /**
     * 统计账单列表（支持与搜索相同的条件）
     *
     * @param dto 搜索条件
     * @return 统计结果
     */
    LedgerListStatsVO statsLedgers(@Param("dto") LedgerSearchDTO dto);
}
