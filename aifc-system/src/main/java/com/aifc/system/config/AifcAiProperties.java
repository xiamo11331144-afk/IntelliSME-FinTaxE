package com.aifc.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aifc.ai")
public class AifcAiProperties
{
    private boolean enabled;
    private String apiKey;
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private String model = "qwen-plus";
    private String visionModel = "qwen-vl-plus";
    private Double temperature = 0.3D;
    private Integer maxTokens = 1024;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVisionModel() { return visionModel; }
    public void setVisionModel(String visionModel) { this.visionModel = visionModel; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
}
