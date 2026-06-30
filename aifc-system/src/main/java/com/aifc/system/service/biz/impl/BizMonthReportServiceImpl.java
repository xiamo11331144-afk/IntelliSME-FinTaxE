package com.aifc.system.service.biz.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.DateUtils;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.domain.biz.BizMonthReport;
import com.aifc.system.mapper.biz.BizMonthReportMapper;
import com.aifc.system.service.biz.IBizMonthReportService;

@Service
public class BizMonthReportServiceImpl implements IBizMonthReportService
{
    @Autowired
    private BizMonthReportMapper bizMonthReportMapper;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Override
    public BizMonthReport selectBizMonthReportById(Long id)
    {
        BizMonthReport report = bizMonthReportMapper.selectBizMonthReportById(id);
        if (report != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(report.getCompanyId());
        }
        return report;
    }

    @Override
    public List<BizMonthReport> selectBizMonthReportList(BizMonthReport bizMonthReport)
    {
        applyCompanyScope(bizMonthReport);
        return bizMonthReportMapper.selectBizMonthReportList(bizMonthReport);
    }

    @Override
    public int insertBizMonthReport(BizMonthReport bizMonthReport)
    {
        validateBasic(bizMonthReport);
        aifcDataScopeService.ensureCompanyAccessible(bizMonthReport.getCompanyId());
        bizMonthReport.setCreateTime(DateUtils.getNowDate());
        return bizMonthReportMapper.insertBizMonthReport(bizMonthReport);
    }

    @Override
    public int updateBizMonthReport(BizMonthReport bizMonthReport)
    {
        BizMonthReport exists = bizMonthReportMapper.selectBizMonthReportById(bizMonthReport.getId());
        if (exists == null)
        {
            throw new ServiceException("month report not found");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        if (bizMonthReport.getCompanyId() != null && !bizMonthReport.getCompanyId().equals(exists.getCompanyId()))
        {
            throw new ServiceException("companyId cannot be changed");
        }
        bizMonthReport.setCompanyId(null);
        bizMonthReport.setUpdateTime(DateUtils.getNowDate());
        return bizMonthReportMapper.updateBizMonthReport(bizMonthReport);
    }

    @Override
    public int deleteBizMonthReportById(Long id)
    {
        BizMonthReport exists = bizMonthReportMapper.selectBizMonthReportById(id);
        if (exists != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        }
        return bizMonthReportMapper.deleteBizMonthReportById(id);
    }

    @Override
    public int deleteBizMonthReportByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                BizMonthReport exists = bizMonthReportMapper.selectBizMonthReportById(id);
                if (exists != null)
                {
                    aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
                }
            }
        }
        return bizMonthReportMapper.deleteBizMonthReportByIds(ids);
    }

    private void validateBasic(BizMonthReport bizMonthReport)
    {
        if (bizMonthReport.getCompanyId() == null)
        {
            throw new ServiceException("companyId is required");
        }
        if (StringUtils.isEmpty(bizMonthReport.getReportMonth()))
        {
            throw new ServiceException("reportMonth is required");
        }
    }

    private void applyCompanyScope(BizMonthReport query)
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
