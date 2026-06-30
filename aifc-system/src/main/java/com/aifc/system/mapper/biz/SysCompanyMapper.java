package com.aifc.system.mapper.biz;

import java.util.List;
import com.aifc.system.domain.biz.SysCompany;

public interface SysCompanyMapper
{
    SysCompany selectSysCompanyById(Long companyId);

    List<SysCompany> selectSysCompanyList(SysCompany sysCompany);

    List<SysCompany> selectSysCompanyByUserId(Long userId);

    int insertSysCompany(SysCompany sysCompany);

    int updateSysCompany(SysCompany sysCompany);

    int deleteSysCompanyById(Long companyId);

    int deleteSysCompanyByIds(Long[] companyIds);
}