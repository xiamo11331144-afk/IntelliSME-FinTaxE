package com.aifc.system.mapper.biz;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.aifc.system.domain.biz.AifcAiMessage;

public interface AifcAiMessageMapper
{
    int insertMessage(AifcAiMessage message);

    List<AifcAiMessage> selectMessageListBySessionId(@Param("sessionId") Long sessionId);

    int deleteMessagesByCreateBy(@Param("createBy") String createBy);
}
