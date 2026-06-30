package com.aifc.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aifc.common.annotation.Log;
import com.aifc.common.core.controller.BaseController;
import com.aifc.common.core.domain.AjaxResult;
import com.aifc.common.core.page.TableDataInfo;
import com.aifc.common.enums.BusinessType;
import com.aifc.system.domain.biz.SysCompany;
import com.aifc.system.service.biz.ISysCompanyService;

@RestController
@RequestMapping("/aifc/company")
public class SysCompanyController extends BaseController
{
    @Autowired
    private ISysCompanyService sysCompanyService;

    /**
     * 查询企业列表 — 无权限拦截，供全局切换器使用
     */
    @GetMapping("/list")
    public TableDataInfo list(SysCompany sysCompany)
    {
        startPage();
        Long userId = getUserId();
        List<SysCompany> list = sysCompanyService.selectCompaniesByUserId(userId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:company:query')")
    @GetMapping(value = "/{companyId}")
    public AjaxResult getInfo(@PathVariable Long companyId)
    {
        return success(sysCompanyService.selectSysCompanyById(companyId));
    }

    @PreAuthorize("@ss.hasPermi('aifc:company:add')")
    @Log(title = "企业信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysCompany sysCompany)
    {
        return toAjax(sysCompanyService.insertSysCompany(sysCompany));
    }

    @PreAuthorize("@ss.hasPermi('aifc:company:edit')")
    @Log(title = "企业信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysCompany sysCompany)
    {
        return toAjax(sysCompanyService.updateSysCompany(sysCompany));
    }

    @PreAuthorize("@ss.hasPermi('aifc:company:remove')")
    @Log(title = "企业信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{companyIds}")
    public AjaxResult remove(@PathVariable Long[] companyIds)
    {
        return toAjax(sysCompanyService.deleteSysCompanyByIds(companyIds));
    }
}