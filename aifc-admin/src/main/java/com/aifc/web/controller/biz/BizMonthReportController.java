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
import com.aifc.system.domain.biz.BizMonthReport;
import com.aifc.system.service.biz.IBizMonthReportService;

@RestController
@RequestMapping("/aifc/monthReport")
public class BizMonthReportController extends BaseController
{
    @Autowired
    private IBizMonthReportService bizMonthReportService;

    @PreAuthorize("@ss.hasPermi('aifc:monthReport:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMonthReport bizMonthReport)
    {
        startPage();
        List<BizMonthReport> list = bizMonthReportService.selectBizMonthReportList(bizMonthReport);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:monthReport:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bizMonthReportService.selectBizMonthReportById(id));
    }

    @PreAuthorize("@ss.hasPermi('aifc:monthReport:add')")
    @Log(title = "AIFC Month Report", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizMonthReport bizMonthReport)
    {
        return toAjax(bizMonthReportService.insertBizMonthReport(bizMonthReport));
    }

    @PreAuthorize("@ss.hasPermi('aifc:monthReport:edit')")
    @Log(title = "AIFC Month Report", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizMonthReport bizMonthReport)
    {
        return toAjax(bizMonthReportService.updateBizMonthReport(bizMonthReport));
    }

    @PreAuthorize("@ss.hasPermi('aifc:monthReport:remove')")
    @Log(title = "AIFC Month Report", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizMonthReportService.deleteBizMonthReportByIds(ids));
    }
}
