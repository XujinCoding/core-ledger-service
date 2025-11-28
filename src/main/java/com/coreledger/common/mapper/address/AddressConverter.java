package com.coreledger.common.mapper.address;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.entity.SysAddress;
import com.coreledger.enums.AddressLevel;
import com.coreledger.vo.address.AddressVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 地址转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface AddressConverter {

    /**
     * Entity 转 VO
     *
     * @param entity 地址实体
     * @return 地址VO
     */
    @Mapping(target = "levelDesc", source = "level", qualifiedByName = "levelToDesc")
    @Mapping(target = "isTopLevel", expression = "java(entity.isTopLevel())")
    @Mapping(target = "isVillageLevel", expression = "java(entity.isVillageLevel())")
    AddressVO toVO(SysAddress entity);

    /**
     * 将层级数值转换为描述
     *
     * @param level 层级数值
     * @return 层级描述
     */
    @Named("levelToDesc")
    default String levelToDesc(Integer level) {
        if (level == null) {
            return null;
        }
        AddressLevel addressLevel = AddressLevel.fromValue(level);
        return addressLevel.getDescription();
    }
}
