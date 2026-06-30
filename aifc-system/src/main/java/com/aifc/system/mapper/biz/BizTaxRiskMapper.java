package com.aifc.system.mapper.biz;

import java.util.List;
import java.util.Map;
import com.aifc.system.domain.biz.BizTaxRisk;

public interface BizTaxRiskMapper
{
    BizTaxRisk selectBizTaxRiskById(Long id);

    List<BizTaxRisk> selectBizTaxRiskList(BizTaxRisk bizTaxRisk);

    int insertBizTaxRisk(BizTaxRisk bizTaxRisk);

    int updateBizTaxRisk(BizTaxRisk bizTaxRisk);

    int deleteBizTaxRiskById(Long id);

    int deleteBizTaxRiskByIds(Long[] ids);

    int deleteBizTaxRiskByCompanyMonth(BizTaxRisk bizTaxRisk);

    BizTaxRisk selectByCompanyMonthAndType(BizTaxRisk bizTaxRisk);

    int deleteByCompanyMonthAndTypeNotIn(BizTaxRisk bizTaxRisk);

    Map<String, Object> selectRiskScanBase(BizTaxRisk bizTaxRisk);

    int countInvoiceTotal(BizTaxRisk bizTaxRisk);

    int countInvoiceAbnormal(BizTaxRisk bizTaxRisk);

    int countVoucherPrivateMix(BizTaxRisk bizTaxRisk);

    int countZeroIncomeMonths(BizTaxRisk bizTaxRisk);

    Map<String, Object> selectInventoryRiskStats(BizTaxRisk bizTaxRisk);

    Map<String, Object> selectSalaryRiskStats(BizTaxRisk bizTaxRisk);

    List<Map<String, Object>> selectCompanyLatestMonths();

    int handleRiskById(BizTaxRisk bizTaxRisk);

    int handleRiskByIds(BizTaxRisk bizTaxRisk);
}
