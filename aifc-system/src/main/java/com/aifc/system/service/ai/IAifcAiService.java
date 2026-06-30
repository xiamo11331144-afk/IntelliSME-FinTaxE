package com.aifc.system.service.ai;

public interface IAifcAiService
{
    String chat(String userPrompt);

    String chat(String systemPrompt, String userPrompt);

    String chatWithImage(String systemPrompt, String userPrompt, String imageUrl);
}
