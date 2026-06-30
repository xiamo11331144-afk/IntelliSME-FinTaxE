package com.aifc.system.service.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizTaxRisk;

public interface IBizTaxRiskService
{
    BizTaxRisk selectBizTaxRiskById(Long id);

    List<BizTaxRisk> selectBizTaxRiskList(BizTaxRisk bizTaxRisk);

    int insertBizTaxRisk(BizTaxRisk bizTaxRisk);

    int updateBizTaxRisk(BizTaxRisk bizTaxRisk);

    int deleteBizTaxRiskById(Long id);

    int deleteBizTaxRiskByIds(Long[] ids);

    int scanRisks(Long companyId, String reportMonth);

    int quickScanRisks(Long companyId, String reportMonth);

    int autoScanLatestMonthForAllCompanies();

    int autoQuickScanLatestMonthForAllCompanies();

    int handleRisk(Long id, Integer status, String handleRemark);

    int handleRiskBatch(Long[] ids, Integer status, String handleRemark);
}
