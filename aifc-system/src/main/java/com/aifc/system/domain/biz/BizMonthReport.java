package com.aifc.system.domain.biz;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

public class BizMonthReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String reportMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalCost;
    private BigDecimal taxPaid;
    private BigDecimal invoiceAmount;
    private BigDecimal salaryTotal;
    private Integer employeeCount;
    private BigDecimal inventoryAmount;
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

    public BigDecimal getTotalIncome()
    {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome)
    {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalCost()
    {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost)
    {
        this.totalCost = totalCost;
    }

    public BigDecimal getTaxPaid()
    {
        return taxPaid;
    }

    public void setTaxPaid(BigDecimal taxPaid)
    {
        this.taxPaid = taxPaid;
    }

    public BigDecimal getInvoiceAmount()
    {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount)
    {
        this.invoiceAmount = invoiceAmount;
    }

    public BigDecimal getSalaryTotal()
    {
        return salaryTotal;
    }

    public void setSalaryTotal(BigDecimal salaryTotal)
    {
        this.salaryTotal = salaryTotal;
    }

    public Integer getEmployeeCount()
    {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount)
    {
        this.employeeCount = employeeCount;
    }

    public BigDecimal getInventoryAmount()
    {
        return inventoryAmount;
    }

    public void setInventoryAmount(BigDecimal inventoryAmount)
    {
        this.inventoryAmount = inventoryAmount;
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
            .append("totalIncome", getTotalIncome())
            .append("totalCost", getTotalCost())
            .append("taxPaid", getTaxPaid())
            .append("invoiceAmount", getInvoiceAmount())
            .append("salaryTotal", getSalaryTotal())
            .append("employeeCount", getEmployeeCount())
            .append("inventoryAmount", getInventoryAmount())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
