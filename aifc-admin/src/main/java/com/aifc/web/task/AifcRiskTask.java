package com.aifc.web.task;

import com.aifc.system.service.biz.IBizTaxRiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aifcRiskTask")
public class AifcRiskTask
{
    private static final Logger log = LoggerFactory.getLogger(AifcRiskTask.class);

    @Autowired
    private IBizTaxRiskService bizTaxRiskService;

    public void dailyScan()
    {
        int inserted = bizTaxRiskService.autoScanLatestMonthForAllCompanies();
        log.info("AIFC risk auto scan finished, inserted={} ", inserted);
    }
}

