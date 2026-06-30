package com.aifc.system.domain.biz;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

public class BizTaxRisk extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String reportMonth;
    private String riskType;
    private String riskLevel;
    private String riskContent;
    private String aiSuggestion;
    private String relatedIds;
    private Integer status;
    private String handleRemark;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getReportMonth() { return reportMonth; }
    public void setReportMonth(String reportMonth) { this.reportMonth = reportMonth; }
    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskContent() { return riskContent; }
    public void setRiskContent(String riskContent) { this.riskContent = riskContent; }
    public String getAiSuggestion() { return aiSuggestion; }
    public void setAiSuggestion(String aiSuggestion) { this.aiSuggestion = aiSuggestion; }
    public String getRelatedIds() { return relatedIds; }
    public void setRelatedIds(String relatedIds) { this.relatedIds = relatedIds; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getHandleRemark() { return handleRemark; }
    public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("companyId", getCompanyId())
            .append("reportMonth", getReportMonth())
            .append("riskType", getRiskType())
            .append("riskLevel", getRiskLevel())
            .append("riskContent", getRiskContent())
            .append("aiSuggestion", getAiSuggestion())
            .append("relatedIds", getRelatedIds())
            .append("status", getStatus())
            .append("handleRemark", getHandleRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
