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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.aifc.common.annotation.Log;
import com.aifc.common.core.controller.BaseController;
import com.aifc.common.core.domain.AjaxResult;
import com.aifc.common.core.page.TableDataInfo;
import com.aifc.common.enums.BusinessType;
import com.aifc.system.domain.biz.BizTaxRisk;
import com.aifc.system.service.biz.IBizTaxRiskService;

@RestController
@RequestMapping("/aifc/taxRisk")
public class BizTaxRiskController extends BaseController
{
    @Autowired
    private IBizTaxRiskService bizTaxRiskService;

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTaxRisk bizTaxRisk)
    {
        startPage();
        List<BizTaxRisk> list = bizTaxRiskService.selectBizTaxRiskList(bizTaxRisk);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bizTaxRiskService.selectBizTaxRiskById(id));
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:add')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTaxRisk bizTaxRisk)
    {
        return toAjax(bizTaxRiskService.insertBizTaxRisk(bizTaxRisk));
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:add')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.OTHER)
    @PostMapping("/scan")
    public AjaxResult scan(@RequestParam Long companyId, @RequestParam String reportMonth)
    {
        int rows = bizTaxRiskService.scanRisks(companyId, reportMonth);
        AjaxResult result = success();
        result.put("scanInserted", rows);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:add')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.OTHER)
    @PostMapping("/scan/auto")
    public AjaxResult scanAuto()
    {
        int rows = bizTaxRiskService.autoScanLatestMonthForAllCompanies();
        AjaxResult result = success();
        result.put("scanInserted", rows);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:add')")
    @Log(title = "AIFC Tax Risk Quick", businessType = BusinessType.OTHER)
    @PostMapping("/scan/quick")
    public AjaxResult scanQuick(@RequestParam Long companyId, @RequestParam String reportMonth)
    {
        int rows = bizTaxRiskService.quickScanRisks(companyId, reportMonth);
        AjaxResult result = success();
        result.put("scanInserted", rows);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:add')")
    @Log(title = "AIFC Tax Risk Quick", businessType = BusinessType.OTHER)
    @PostMapping("/scan/auto/quick")
    public AjaxResult scanAutoQuick()
    {
        int rows = bizTaxRiskService.autoQuickScanLatestMonthForAllCompanies();
        AjaxResult result = success();
        result.put("scanInserted", rows);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:edit')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/handle")
    public AjaxResult handle(@PathVariable Long id, @RequestBody HandleRequest request)
    {
        return toAjax(bizTaxRiskService.handleRisk(id, request.getStatus(), request.getHandleRemark()));
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:edit')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.UPDATE)
    @PostMapping("/handle/batch")
    public AjaxResult handleBatch(@RequestBody BatchHandleRequest request)
    {
        return toAjax(bizTaxRiskService.handleRiskBatch(
            request.getIds(),
            request.getStatus(),
            request.getHandleRemark()));
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:edit')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTaxRisk bizTaxRisk)
    {
        return toAjax(bizTaxRiskService.updateBizTaxRisk(bizTaxRisk));
    }

    @PreAuthorize("@ss.hasPermi('aifc:taxRisk:remove')")
    @Log(title = "AIFC Tax Risk", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizTaxRiskService.deleteBizTaxRiskByIds(ids));
    }

    public static class HandleRequest
    {
        private Integer status;
        private String handleRemark;

        public Integer getStatus()
        {
            return status;
        }

        public void setStatus(Integer status)
        {
            this.status = status;
        }

        public String getHandleRemark()
        {
            return handleRemark;
        }

        public void setHandleRemark(String handleRemark)
        {
            this.handleRemark = handleRemark;
        }
    }

    public static class BatchHandleRequest
    {
        private Long[] ids;
        private Integer status;
        private String handleRemark;

        public Long[] getIds()
        {
            return ids;
        }

        public void setIds(Long[] ids)
        {
            this.ids = ids;
        }

        public Integer getStatus()
        {
            return status;
        }

        public void setStatus(Integer status)
        {
            this.status = status;
        }

        public String getHandleRemark()
        {
            return handleRemark;
        }

        public void setHandleRemark(String handleRemark)
        {
            this.handleRemark = handleRemark;
        }
    }
}
