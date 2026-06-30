package com.aifc.system.service.biz;

import java.util.List;
import java.util.Map;

public interface IBizAnalysisService
{
    Map<String, Object> summary(Long companyId, String periodType, String period);

    List<Map<String, Object>> trend(Long companyId, Integer months);
}
