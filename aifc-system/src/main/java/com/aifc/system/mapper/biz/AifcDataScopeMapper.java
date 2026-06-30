package com.aifc.system.mapper.biz;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AifcDataScopeMapper
{
    List<Long> selectAccessibleCompanyIds(@Param("userId") Long userId);
}

