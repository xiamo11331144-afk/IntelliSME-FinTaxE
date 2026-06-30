package com.aifc.system.domain.biz;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

public class BizInvoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String reportMonth;
    private String invoiceCode;
    private String invoiceNo;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private Date invoiceDate;
    private String sellerName;
    private String buyerName;
    private String goodsName;
    private Integer isInteger;
    private Integer isSeries;
    private String fileUrl;
    private String fileType;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getReportMonth() { return reportMonth; }
    public void setReportMonth(String reportMonth) { this.reportMonth = reportMonth; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public Date getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public Integer getIsInteger() { return isInteger; }
    public void setIsInteger(Integer isInteger) { this.isInteger = isInteger; }
    public Integer getIsSeries() { return isSeries; }
    public void setIsSeries(Integer isSeries) { this.isSeries = isSeries; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("companyId", getCompanyId())
            .append("reportMonth", getReportMonth())
            .append("invoiceCode", getInvoiceCode())
            .append("invoiceNo", getInvoiceNo())
            .append("amount", getAmount())
            .append("taxAmount", getTaxAmount())
            .append("invoiceDate", getInvoiceDate())
            .append("sellerName", getSellerName())
            .append("buyerName", getBuyerName())
            .append("goodsName", getGoodsName())
            .append("isInteger", getIsInteger())
            .append("isSeries", getIsSeries())
            .append("fileUrl", getFileUrl())
            .append("fileType", getFileType())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
