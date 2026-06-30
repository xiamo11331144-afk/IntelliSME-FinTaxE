package com.aifc.system.mapper.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizMonthReport;

public interface BizMonthReportMapper
{
    BizMonthReport selectBizMonthReportById(Long id);

    List<BizMonthReport> selectBizMonthReportList(BizMonthReport bizMonthReport);

    int insertBizMonthReport(BizMonthReport bizMonthReport);

    int updateBizMonthReport(BizMonthReport bizMonthReport);

    int deleteBizMonthReportById(Long id);

    int deleteBizMonthReportByIds(Long[] ids);
}
