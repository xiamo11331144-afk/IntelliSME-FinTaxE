package com.aifc.system.domain.biz;

import java.util.Date;
import com.aifc.common.core.domain.BaseEntity;

public class AifcAiSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String systemPrompt;
    private String lastMessage;
    private Date lastChatTime;
    private Integer status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage)
    {
        this.lastMessage = lastMessage;
    }

    public Date getLastChatTime()
    {
        return lastChatTime;
    }

    public void setLastChatTime(Date lastChatTime)
    {
        this.lastChatTime = lastChatTime;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }
}

