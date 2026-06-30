package com.aifc.system.domain.biz;

import com.aifc.common.core.domain.BaseEntity;

public class AifcAiMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sessionId;
    private String role;
    private String content;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}

