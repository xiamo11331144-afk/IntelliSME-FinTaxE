package com.aifc.system.mapper.biz;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.aifc.system.domain.biz.AifcAiSession;

public interface AifcAiSessionMapper
{
    int insertSession(AifcAiSession session);

    int updateSessionMeta(AifcAiSession session);

    AifcAiSession selectSessionByIdAndCreateBy(@Param("id") Long id, @Param("createBy") String createBy);

    List<AifcAiSession> selectSessionListByCreateBy(@Param("createBy") String createBy);

    int clearSessionsByCreateBy(@Param("createBy") String createBy, @Param("updateBy") String updateBy);
}
