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
import com.aifc.system.domain.biz.BizSalary;
import com.aifc.system.service.biz.IBizSalaryService;

@RestController
@RequestMapping("/aifc/salary")
public class BizSalaryController extends BaseController
{
    @Autowired
    private IBizSalaryService bizSalaryService;

    @PreAuthorize("@ss.hasPermi('aifc:salary:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizSalary bizSalary)
    {
        startPage();
        List<BizSalary> list = bizSalaryService.selectBizSalaryList(bizSalary);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:salary:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bizSalaryService.selectBizSalaryById(id));
    }

    @PreAuthorize("@ss.hasPermi('aifc:salary:add')")
    @Log(title = "AIFC Salary", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizSalary bizSalary)
    {
        return toAjax(bizSalaryService.insertBizSalary(bizSalary));
    }

    @PreAuthorize("@ss.hasPermi('aifc:salary:edit')")
    @Log(title = "AIFC Salary", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizSalary bizSalary)
    {
        return toAjax(bizSalaryService.updateBizSalary(bizSalary));
    }

    @PreAuthorize("@ss.hasPermi('aifc:salary:remove')")
    @Log(title = "AIFC Salary", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizSalaryService.deleteBizSalaryByIds(ids));
    }
}
