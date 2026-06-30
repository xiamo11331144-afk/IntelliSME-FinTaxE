package com.aifc.system.service.biz.impl;

import java.util.Collections;
import java.util.List;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.SecurityUtils;
import com.aifc.system.mapper.biz.AifcDataScopeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AifcDataScopeService
{
    @Autowired
    private AifcDataScopeMapper aifcDataScopeMapper;

    public List<Long> getAccessibleCompanyIds()
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId))
        {
            return Collections.emptyList();
        }
        return aifcDataScopeMapper.selectAccessibleCompanyIds(userId);
    }

    public void ensureCompanyAccessible(Long companyId)
    {
        if (companyId == null)
        {
            throw new ServiceException("companyId is required");
        }
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId))
        {
            return;
        }
        List<Long> companyIds = aifcDataScopeMapper.selectAccessibleCompanyIds(userId);
        if (companyIds == null || companyIds.isEmpty() || !companyIds.contains(companyId))
        {
            throw new ServiceException("no permission for companyId=" + companyId);
        }
    }
}

