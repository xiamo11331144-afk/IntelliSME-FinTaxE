package com.aifc.system.service.ai.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.config.AifcAiProperties;
import com.aifc.system.service.ai.IAifcAiService;

@Service
public class QwenAiServiceImpl implements IAifcAiService
{
    private static final Logger log = LoggerFactory.getLogger(QwenAiServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final AifcAiProperties properties;

    public QwenAiServiceImpl(AifcAiProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public String chat(String userPrompt)
    {
        return chat("You are a finance and tax assistant for SME businesses.", userPrompt);
    }

    @Override
    public String chat(String systemPrompt, String userPrompt)
    {
        return doChat(systemPrompt, userPrompt, null);
    }

    @Override
    public String chatWithImage(String systemPrompt, String userPrompt, String imageUrl)
    {
        return doChat(systemPrompt, userPrompt, imageUrl);
    }

    private String doChat(String systemPrompt, String userPrompt, String imageUrl)
    {
        if (!properties.isEnabled())
        {
            return "AI service is disabled. Enable `aifc.ai.enabled=true` first.";
        }
        if (StringUtils.isEmpty(properties.getApiKey()))
        {
            return "AI API key is missing. Configure `aifc.ai.api-key`.";
        }
        if (StringUtils.isEmpty(userPrompt))
        {
            return "Prompt is empty.";
        }

        JSONObject req = new JSONObject();
        req.put("model", StringUtils.isNotEmpty(imageUrl) ? properties.getVisionModel() : properties.getModel());
        req.put("temperature", properties.getTemperature());
        req.put("max_tokens", properties.getMaxTokens());

        JSONArray messages = new JSONArray();
        messages.add(message("system", systemPrompt));
        if (StringUtils.isNotEmpty(imageUrl))
        {
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();

            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", imageUrl);
            imagePart.put("image_url", imageUrlObj);
            content.add(imagePart);

            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", userPrompt);
            content.add(textPart);

            userMessage.put("content", content);
            messages.add(userMessage);
        }
        else
        {
            messages.add(message("user", userPrompt));
        }
        req.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        try
        {
            String response = restTemplate.postForObject(properties.getBaseUrl(), new HttpEntity<>(req.toJSONString(), headers), String.class);
            return extractContent(response);
        }
        catch (Exception e)
        {
            log.error("Call qwen failed", e);
            return "AI service call failed: " + e.getMessage();
        }
    }

    private JSONObject message(String role, String content)
    {
        JSONObject item = new JSONObject();
        item.put("role", role);
        item.put("content", content);
        return item;
    }

    private String extractContent(String response)
    {
        if (StringUtils.isEmpty(response))
        {
            return "Empty AI response.";
        }
        JSONObject root = JSONObject.parseObject(response);
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            return response;
        }
        JSONObject choice0 = choices.getJSONObject(0);
        if (choice0 == null)
        {
            return response;
        }
        JSONObject message = choice0.getJSONObject("message");
        if (message == null)
        {
            return response;
        }
        String content = message.getString("content");
        return StringUtils.isEmpty(content) ? response : content;
    }
}
