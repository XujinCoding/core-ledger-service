package com.coreledger.common.mapper.sysuser;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.security.UserPrincipal;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import org.mapstruct.Mapper;

@Mapper(config = BeanMapperConf.class)
public interface SysUserConvert {
    /**
     * 转换成Principal
     *
     * @param entity 实体
     * @return VO
     */
    UserPrincipal toPrincipal(CurrentUserIdentityInfo entity);

    CurrentUserIdentityInfo toInfo(UserPrincipal entity);
}
