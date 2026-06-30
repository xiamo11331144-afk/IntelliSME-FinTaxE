package com.aifc.web.controller.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aifc.common.annotation.Log;
import com.aifc.common.core.controller.BaseController;
import com.aifc.common.core.domain.AjaxResult;
import com.aifc.common.enums.BusinessType;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.service.biz.IAifcAssistantService;
import com.aifc.system.service.biz.IAifcAssistantService.ChatResult;

@RestController
@RequestMapping("/aifc/ai")
public class AifcAiController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AifcAiController.class);

    @Autowired
    private IAifcAssistantService assistantService;

    @PreAuthorize("@ss.hasAnyPermi('aifc:ai:chat,aifc:assistant:list,aifc:assistant:query')")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatRequest request)
    {
        ChatResult chatResult = assistantService.chat(
            request.getSessionId(),
            request.getSystemPrompt(),
            request.getUserPrompt(),
            getUsername());
        String result = chatResult.getAnswer();
        log.info("[ai-chat] promptLen={}, resultLen={}",
            request.getUserPrompt() == null ? 0 : request.getUserPrompt().length(),
            result == null ? 0 : result.length());
        if (StringUtils.isEmpty(result))
        {
            return AjaxResult.error("AI response is empty, please check model settings and retry");
        }
        return success(chatResult);
    }

    @PreAuthorize("@ss.hasAnyPermi('aifc:ai:chat,aifc:assistant:list,aifc:assistant:query')")
    @GetMapping("/sessions")
    public AjaxResult sessions()
    {
        return success(assistantService.listSessions(getUsername()));
    }

    @PreAuthorize("@ss.hasAnyPermi('aifc:ai:chat,aifc:assistant:list,aifc:assistant:query')")
    @GetMapping("/sessions/{sessionId}/messages")
    public AjaxResult messages(@PathVariable("sessionId") Long sessionId)
    {
        return success(assistantService.listMessages(sessionId, getUsername()));
    }

    @PreAuthorize("@ss.hasAnyPermi('aifc:ai:chat,aifc:assistant:list,aifc:assistant:query')")
    @PostMapping("/sessions")
    public AjaxResult createSession(@RequestBody(required = false) SessionCreateRequest request)
    {
        SessionCreateRequest req = request == null ? new SessionCreateRequest() : request;
        return success(assistantService.createSession(req.getTitle(), req.getSystemPrompt(), getUsername()));
    }

    @PreAuthorize("@ss.hasAnyPermi('aifc:ai:chat,aifc:assistant:list,aifc:assistant:query')")
    @Log(title = "AI Assistant", businessType = BusinessType.CLEAN)
    @DeleteMapping("/sessions/clear-all")
    public AjaxResult clearAllSessions()
    {
        int cleared = assistantService.clearAllSessions(getUsername());
        return success().put("cleared", cleared);
    }

    public static class ChatRequest
    {
        private Long sessionId;
        private String systemPrompt;
        private String userPrompt;

        public Long getSessionId()
        {
            return sessionId;
        }

        public void setSessionId(Long sessionId)
        {
            this.sessionId = sessionId;
        }

        public String getSystemPrompt()
        {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt)
        {
            this.systemPrompt = systemPrompt;
        }

        public String getUserPrompt()
        {
            return userPrompt;
        }

        public void setUserPrompt(String userPrompt)
        {
            this.userPrompt = userPrompt;
        }
    }

    public static class SessionCreateRequest
    {
        private String title;
        private String systemPrompt;

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getSystemPrompt()
        {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt)
        {
            this.systemPrompt = systemPrompt;
        }
    }
}
