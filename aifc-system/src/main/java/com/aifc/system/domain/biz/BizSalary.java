package com.aifc.system.domain.biz;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

public class BizSalary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String reportMonth;
    private String employeeName;
    private BigDecimal salaryAmount;
    private Integer is5000;
    private BigDecimal taxDeducted;

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

    public String getEmployeeName()
    {
        return employeeName;
    }

    public void setEmployeeName(String employeeName)
    {
        this.employeeName = employeeName;
    }

    public BigDecimal getSalaryAmount()
    {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount)
    {
        this.salaryAmount = salaryAmount;
    }

    public Integer getIs5000()
    {
        return is5000;
    }

    public void setIs5000(Integer is5000)
    {
        this.is5000 = is5000;
    }

    public BigDecimal getTaxDeducted()
    {
        return taxDeducted;
    }

    public void setTaxDeducted(BigDecimal taxDeducted)
    {
        this.taxDeducted = taxDeducted;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("companyId", getCompanyId())
            .append("reportMonth", getReportMonth())
            .append("employeeName", getEmployeeName())
            .append("salaryAmount", getSalaryAmount())
            .append("is5000", getIs5000())
            .append("taxDeducted", getTaxDeducted())
            .append("createTime", getCreateTime())
            .toString();
    }
}
