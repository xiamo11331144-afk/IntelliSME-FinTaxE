package com.aifc.system.domain.biz;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aifc.common.core.domain.BaseEntity;

/**
 * 企业信息 sys_company
 */
public class SysCompany extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 企业ID */
    private Long companyId;

    /** 企业名称 */
    private String companyName;

    /** 所属行业 */
    private String industry;

    /** 纳税人类型：1-小规模 2-一般纳税人 */
    private Integer taxpayerType;

    /** 企业地址 */
    private String address;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 企业所有者用户ID */
    private Long ownerUserId;

    /** 更新时间 */
    private Date updateTime;

    public Long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(Long companyId)
    {
        this.companyId = companyId;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public Integer getTaxpayerType()
    {
        return taxpayerType;
    }

    public void setTaxpayerType(Integer taxpayerType)
    {
        this.taxpayerType = taxpayerType;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Long getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId)
    {
        this.ownerUserId = ownerUserId;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("companyId", getCompanyId())
            .append("companyName", getCompanyName())
            .append("industry", getIndustry())
            .append("taxpayerType", getTaxpayerType())
            .append("address", getAddress())
            .append("contact", getContact())
            .append("phone", getPhone())
            .append("ownerUserId", getOwnerUserId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}