package com.aifc.system.service.biz.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.DateUtils;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.domain.biz.BizSalary;
import com.aifc.system.mapper.biz.BizSalaryMapper;
import com.aifc.system.service.biz.IBizSalaryService;

@Service
public class BizSalaryServiceImpl implements IBizSalaryService
{
    @Autowired
    private BizSalaryMapper bizSalaryMapper;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Override
    public BizSalary selectBizSalaryById(Long id)
    {
        BizSalary salary = bizSalaryMapper.selectBizSalaryById(id);
        if (salary != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(salary.getCompanyId());
        }
        return salary;
    }

    @Override
    public List<BizSalary> selectBizSalaryList(BizSalary bizSalary)
    {
        applyCompanyScope(bizSalary);
        return bizSalaryMapper.selectBizSalaryList(bizSalary);
    }

    @Override
    public int insertBizSalary(BizSalary bizSalary)
    {
        validateBasic(bizSalary);
        aifcDataScopeService.ensureCompanyAccessible(bizSalary.getCompanyId());
        if (bizSalary.getIs5000() == null)
        {
            bizSalary.setIs5000(0);
        }
        if (bizSalary.getTaxDeducted() == null)
        {
            bizSalary.setTaxDeducted(java.math.BigDecimal.ZERO);
        }
        bizSalary.setCreateTime(DateUtils.getNowDate());
        return bizSalaryMapper.insertBizSalary(bizSalary);
    }

    @Override
    public int updateBizSalary(BizSalary bizSalary)
    {
        BizSalary exists = bizSalaryMapper.selectBizSalaryById(bizSalary.getId());
        if (exists == null)
        {
            throw new ServiceException("salary not found");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        if (bizSalary.getCompanyId() != null && !bizSalary.getCompanyId().equals(exists.getCompanyId()))
        {
            throw new ServiceException("companyId cannot be changed");
        }
        bizSalary.setCompanyId(null);
        return bizSalaryMapper.updateBizSalary(bizSalary);
    }

    @Override
    public int deleteBizSalaryById(Long id)
    {
        BizSalary exists = bizSalaryMapper.selectBizSalaryById(id);
        if (exists != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        }
        return bizSalaryMapper.deleteBizSalaryById(id);
    }

    @Override
    public int deleteBizSalaryByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                BizSalary exists = bizSalaryMapper.selectBizSalaryById(id);
                if (exists != null)
                {
                    aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
                }
            }
        }
        return bizSalaryMapper.deleteBizSalaryByIds(ids);
    }

    private void validateBasic(BizSalary bizSalary)
    {
        if (bizSalary.getCompanyId() == null)
        {
            throw new ServiceException("companyId is required");
        }
        if (StringUtils.isEmpty(bizSalary.getReportMonth()))
        {
            throw new ServiceException("reportMonth is required");
        }
        if (StringUtils.isEmpty(bizSalary.getEmployeeName()))
        {
            throw new ServiceException("employeeName is required");
        }
        if (bizSalary.getSalaryAmount() == null)
        {
            throw new ServiceException("salaryAmount is required");
        }
    }

    private void applyCompanyScope(BizSalary query)
    {
        if (query == null)
        {
            return;
        }
        if (query.getCompanyId() != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(query.getCompanyId());
            return;
        }
        List<Long> companyIds = aifcDataScopeService.getAccessibleCompanyIds();
        if (companyIds != null && !companyIds.isEmpty())
        {
            query.getParams().put("allowedCompanyIds", companyIds);
        }
    }
}
