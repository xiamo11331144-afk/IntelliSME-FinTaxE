package com.aifc.web.controller.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.aifc.common.core.controller.BaseController;
import com.aifc.common.core.domain.AjaxResult;
import com.aifc.system.service.biz.IBizAnalysisService;

@RestController
@RequestMapping("/aifc/analysis")
public class BizAnalysisController extends BaseController
{
    @Autowired
    private IBizAnalysisService bizAnalysisService;

    @PreAuthorize("@ss.hasPermi('aifc:analysis:list')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) Long companyId,
                              @RequestParam String periodType,
                              @RequestParam String period)
    {
        return success(bizAnalysisService.summary(companyId, periodType, period));
    }

    @PreAuthorize("@ss.hasPermi('aifc:analysis:list')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(required = false) Long companyId,
                            @RequestParam(required = false) Integer months)
    {
        return success(bizAnalysisService.trend(companyId, months));
    }
}
