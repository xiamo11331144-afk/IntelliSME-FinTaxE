package com.aifc.system.service.biz;

import java.util.List;
import com.aifc.system.domain.biz.AifcAiMessage;
import com.aifc.system.domain.biz.AifcAiSession;

public interface IAifcAssistantService
{
    AifcAiSession createSession(String title, String systemPrompt, String username);

    List<AifcAiSession> listSessions(String username);

    List<AifcAiMessage> listMessages(Long sessionId, String username);

    ChatResult chat(Long sessionId, String systemPrompt, String userPrompt, String username);

    int clearAllSessions(String username);

    class ChatResult
    {
        private Long sessionId;
        private String answer;

        public ChatResult(Long sessionId, String answer)
        {
            this.sessionId = sessionId;
            this.answer = answer;
        }

        public Long getSessionId()
        {
            return sessionId;
        }

        public String getAnswer()
        {
            return answer;
        }
    }
}
