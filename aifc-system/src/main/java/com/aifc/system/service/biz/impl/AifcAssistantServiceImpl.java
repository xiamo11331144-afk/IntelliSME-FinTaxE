package com.aifc.system.service.biz.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.domain.biz.AifcAiMessage;
import com.aifc.system.domain.biz.AifcAiSession;
import com.aifc.system.mapper.biz.AifcAiMessageMapper;
import com.aifc.system.mapper.biz.AifcAiSessionMapper;
import com.aifc.system.service.ai.IAifcAiService;
import com.aifc.system.service.biz.IAifcAssistantService;

@Service
public class AifcAssistantServiceImpl implements IAifcAssistantService
{
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a finance and tax assistant for SME businesses. Reply in concise Chinese.";

    @Autowired
    private AifcAiSessionMapper sessionMapper;

    @Autowired
    private AifcAiMessageMapper messageMapper;

    @Autowired
    private IAifcAiService aifcAiService;

    @Override
    public AifcAiSession createSession(String title, String systemPrompt, String username)
    {
        AifcAiSession session = new AifcAiSession();
        session.setTitle(normalizeTitle(title));
        session.setSystemPrompt(normalizeSystemPrompt(systemPrompt));
        session.setLastMessage("");
        session.setLastChatTime(new Date());
        session.setStatus(1);
        session.setCreateBy(username);
        session.setUpdateBy(username);
        sessionMapper.insertSession(session);
        return session;
    }

    @Override
    public List<AifcAiSession> listSessions(String username)
    {
        return sessionMapper.selectSessionListByCreateBy(username);
    }

    @Override
    public List<AifcAiMessage> listMessages(Long sessionId, String username)
    {
        AifcAiSession session = requireSession(sessionId, username);
        return messageMapper.selectMessageListBySessionId(session.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearAllSessions(String username)
    {
        messageMapper.deleteMessagesByCreateBy(username);
        return sessionMapper.clearSessionsByCreateBy(username, username);
    }

    @Override
    public ChatResult chat(Long sessionId, String systemPrompt, String userPrompt, String username)
    {
        if (StringUtils.isEmpty(userPrompt))
        {
            throw new ServiceException("Please input a question before sending");
        }

        String prompt = normalizeSystemPrompt(systemPrompt);
        AifcAiSession session = sessionId == null ? null : sessionMapper.selectSessionByIdAndCreateBy(sessionId, username);
        if (session == null)
        {
            session = createSession(userPrompt, prompt, username);
        }
        else
        {
            prompt = StringUtils.isNotEmpty(systemPrompt) ? systemPrompt : normalizeSystemPrompt(session.getSystemPrompt());
        }

        insertMessage(session.getId(), "user", userPrompt, username);
        String answer = aifcAiService.chat(prompt, userPrompt);
        insertMessage(session.getId(), "assistant", answer, username);

        AifcAiSession update = new AifcAiSession();
        update.setId(session.getId());
        update.setCreateBy(username);
        update.setUpdateBy(username);
        update.setSystemPrompt(prompt);
        update.setLastMessage(cut(answer, 180));
        update.setLastChatTime(new Date());
        if (StringUtils.isEmpty(session.getTitle()) || "New Chat".equals(session.getTitle()))
        {
            update.setTitle(cut(userPrompt, 30));
        }
        sessionMapper.updateSessionMeta(update);

        return new ChatResult(session.getId(), answer);
    }

    private AifcAiSession requireSession(Long sessionId, String username)
    {
        if (sessionId == null)
        {
            throw new ServiceException("Session ID is required");
        }
        AifcAiSession session = sessionMapper.selectSessionByIdAndCreateBy(sessionId, username);
        if (session == null)
        {
            throw new ServiceException("Session does not exist or access is denied");
        }
        return session;
    }

    private void insertMessage(Long sessionId, String role, String content, String username)
    {
        AifcAiMessage message = new AifcAiMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateBy(username);
        message.setUpdateBy(username);
        messageMapper.insertMessage(message);
    }

    private String normalizeTitle(String title)
    {
        if (StringUtils.isEmpty(title))
        {
            return "New Chat";
        }
        return cut(title.trim(), 50);
    }

    private String normalizeSystemPrompt(String systemPrompt)
    {
        return StringUtils.isEmpty(systemPrompt) ? DEFAULT_SYSTEM_PROMPT : systemPrompt.trim();
    }

    private String cut(String text, int maxLen)
    {
        if (StringUtils.isEmpty(text))
        {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
