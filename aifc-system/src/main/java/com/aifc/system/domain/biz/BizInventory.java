package com.aifc.system.domain.biz;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

public class BizInventory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String reportMonth;
    private BigDecimal bookAmount;
    private BigDecimal realAmount;
    private BigDecimal diffAmount;
    private BigDecimal diffRate;
    private String remark;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(Long companyId)
    {
        this.companyId = companyId;
    }

    public String getReportMonth()
    {
        return reportMonth;
    }

    public void setReportMonth(String reportMonth)
    {
        this.reportMonth = reportMonth;
    }

    public BigDecimal getBookAmount()
    {
        return bookAmount;
    }

    public void setBookAmount(BigDecimal bookAmount)
    {
        this.bookAmount = bookAmount;
    }

    public BigDecimal getRealAmount()
    {
        return realAmount;
    }

    public void setRealAmount(BigDecimal realAmount)
    {
        this.realAmount = realAmount;
    }

    public BigDecimal getDiffAmount()
    {
        return diffAmount;
    }

    public void setDiffAmount(BigDecimal diffAmount)
    {
        this.diffAmount = diffAmount;
    }

    public BigDecimal getDiffRate()
    {
        return diffRate;
    }

    public void setDiffRate(BigDecimal diffRate)
    {
        this.diffRate = diffRate;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("companyId", getCompanyId())
            .append("reportMonth", getReportMonth())
            .append("bookAmount", getBookAmount())
            .append("realAmount", getRealAmount())
            .append("diffAmount", getDiffAmount())
            .append("diffRate", getDiffRate())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
