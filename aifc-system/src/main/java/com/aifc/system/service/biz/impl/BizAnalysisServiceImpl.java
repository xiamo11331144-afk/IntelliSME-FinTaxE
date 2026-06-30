package com.aifc.system.service.biz.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.mapper.biz.BizAnalysisMapper;
import com.aifc.system.service.biz.IBizAnalysisService;

@Service
public class BizAnalysisServiceImpl implements IBizAnalysisService
{
    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private BizAnalysisMapper bizAnalysisMapper;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Override
    public Map<String, Object> summary(Long companyId, String periodType, String period)
    {
        if (StringUtils.isEmpty(periodType) || StringUtils.isEmpty(period))
        {
            throw new ServiceException("periodType and period are required");
        }

        String[] range = resolveRange(periodType, period);
        Map<String, Object> params = new HashMap<>();
        applyCompanyScope(params, companyId);
        params.put("startMonth", range[0]);
        params.put("endMonth", range[1]);

        Map<String, Object> summary = bizAnalysisMapper.selectSummaryByRange(params);
        BigDecimal totalIncome = toBigDecimal(summary.get("totalIncome"));
        BigDecimal totalCost = toBigDecimal(summary.get("totalCost"));
        BigDecimal taxPaid = toBigDecimal(summary.get("taxPaid"));
        BigDecimal invoiceAmount = toBigDecimal(summary.get("invoiceAmount"));
        BigDecimal netProfit = totalIncome.subtract(totalCost);
        BigDecimal profitRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0)
        {
            profitRate = netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("companyId", companyId);
        result.put("periodType", periodType);
        result.put("period", period);
        result.put("startMonth", range[0]);
        result.put("endMonth", range[1]);
        result.put("totalIncome", totalIncome);
        result.put("totalCost", totalCost);
        result.put("taxPaid", taxPaid);
        result.put("invoiceAmount", invoiceAmount);
        result.put("netProfit", netProfit);
        result.put("profitRate", profitRate);
        result.put("riskCount", bizAnalysisMapper.selectRiskCountByRange(params));
        result.put("invoiceCount", bizAnalysisMapper.selectInvoiceCountByRange(params));
        return result;
    }

    @Override
    public List<Map<String, Object>> trend(Long companyId, Integer months)
    {
        int n = (months == null || months <= 0) ? 6 : Math.min(months, 24);
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(n - 1L);

        Map<String, Object> params = new HashMap<>();
        applyCompanyScope(params, companyId);
        params.put("startMonth", start.format(YM_FMT));
        params.put("endMonth", end.format(YM_FMT));
        return bizAnalysisMapper.selectTrendByRange(params);
    }

    private void applyCompanyScope(Map<String, Object> params, Long companyId)
    {
        if (companyId != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(companyId);
            params.put("companyId", companyId);
            return;
        }
        List<Long> companyIds = aifcDataScopeService.getAccessibleCompanyIds();
        if (companyIds != null && !companyIds.isEmpty())
        {
            params.put("allowedCompanyIds", companyIds);
        }
    }

    private String[] resolveRange(String periodType, String period)
    {
        String type = periodType.toLowerCase();
        if ("month".equals(type))
        {
            validateMonth(period);
            return new String[] { period, period };
        }
        if ("quarter".equals(type))
        {
            String[] parts = period.split("-Q");
            if (parts.length != 2)
            {
                throw new ServiceException("quarter format should be yyyy-Qn");
            }
            int year;
            int q;
            try
            {
                year = Integer.parseInt(parts[0]);
                q = Integer.parseInt(parts[1]);
            }
            catch (Exception e)
            {
                throw new ServiceException("quarter format should be yyyy-Qn");
            }
            if (q < 1 || q > 4)
            {
                throw new ServiceException("quarter should be Q1~Q4");
            }
            int startMonth = (q - 1) * 3 + 1;
            int endMonth = startMonth + 2;
            return new String[] {
                String.format("%04d-%02d", year, startMonth),
                String.format("%04d-%02d", year, endMonth)
            };
        }
        if ("year".equals(type))
        {
            int year;
            try
            {
                year = Integer.parseInt(period);
            }
            catch (Exception e)
            {
                throw new ServiceException("year format should be yyyy");
            }
            return new String[] {
                String.format("%04d-01", year),
                String.format("%04d-12", year)
            };
        }
        throw new ServiceException("periodType should be month/quarter/year");
    }

    private void validateMonth(String period)
    {
        try
        {
            YearMonth.parse(period, YM_FMT);
        }
        catch (Exception e)
        {
            throw new ServiceException("month format should be yyyy-MM");
        }
    }

    private BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        try
        {
            return new BigDecimal(String.valueOf(value));
        }
        catch (Exception e)
        {
            return BigDecimal.ZERO;
        }
    }
}
