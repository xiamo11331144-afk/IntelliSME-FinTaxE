package com.aifc.system.service.biz;

import java.util.List;
import com.aifc.system.domain.biz.SysCompany;

public interface ISysCompanyService
{
    SysCompany selectSysCompanyById(Long companyId);

    List<SysCompany> selectSysCompanyList(SysCompany sysCompany);

    /**
     * 查询当前用户可访问的企业列表（通过 sys_user_company 关联）
     */
    List<SysCompany> selectCompaniesByUserId(Long userId);

    int insertSysCompany(SysCompany sysCompany);

    int updateSysCompany(SysCompany sysCompany);

    int deleteSysCompanyById(Long companyId);

    int deleteSysCompanyByIds(Long[] companyIds);
}