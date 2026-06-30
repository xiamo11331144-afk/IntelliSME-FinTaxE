package com.aifc.system.mapper.biz;

import java.util.List;
import java.util.Map;

public interface BizAnalysisMapper
{
    Map<String, Object> selectSummaryByRange(Map<String, Object> params);

    int selectRiskCountByRange(Map<String, Object> params);

    int selectInvoiceCountByRange(Map<String, Object> params);

    List<Map<String, Object>> selectTrendByRange(Map<String, Object> params);
}
