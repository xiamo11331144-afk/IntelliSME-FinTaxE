package com.aifc.system.service.biz.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aifc.system.domain.biz.SysCompany;
import com.aifc.system.mapper.biz.SysCompanyMapper;
import com.aifc.system.service.biz.ISysCompanyService;

@Service
public class SysCompanyServiceImpl implements ISysCompanyService
{
    @Autowired
    private SysCompanyMapper sysCompanyMapper;

    @Override
    public SysCompany selectSysCompanyById(Long companyId)
    {
        return sysCompanyMapper.selectSysCompanyById(companyId);
    }

    @Override
    public List<SysCompany> selectSysCompanyList(SysCompany sysCompany)
    {
        return sysCompanyMapper.selectSysCompanyList(sysCompany);
    }

    @Override
    public List<SysCompany> selectCompaniesByUserId(Long userId)
    {
        List<SysCompany> list = sysCompanyMapper.selectSysCompanyByUserId(userId);
        if (list == null || list.isEmpty())
        {
            // 用户未关联企业时返回全部（管理员场景）
            return sysCompanyMapper.selectSysCompanyList(new SysCompany());
        }
        return list;
    }

    @Override
    public int insertSysCompany(SysCompany sysCompany)
    {
        return sysCompanyMapper.insertSysCompany(sysCompany);
    }

    @Override
    public int updateSysCompany(SysCompany sysCompany)
    {
        return sysCompanyMapper.updateSysCompany(sysCompany);
    }

    @Override
    public int deleteSysCompanyById(Long companyId)
    {
        return sysCompanyMapper.deleteSysCompanyById(companyId);
    }

    @Override
    public int deleteSysCompanyByIds(Long[] companyIds)
    {
        return sysCompanyMapper.deleteSysCompanyByIds(companyIds);
    }
}