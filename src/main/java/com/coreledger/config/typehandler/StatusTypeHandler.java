package com.coreledger.config.typehandler;

import com.coreledger.enums.Status;
import com.coreledger.enums.UserRole;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;



@MappedTypes(Status.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class StatusTypeHandler extends BaseEnumTypeHandler<Status> {

    public StatusTypeHandler() {
        super(Status.class);
    }
}

