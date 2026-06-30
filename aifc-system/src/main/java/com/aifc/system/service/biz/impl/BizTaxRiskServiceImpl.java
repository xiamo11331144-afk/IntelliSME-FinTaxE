package com.aifc.system.service.biz.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.DateUtils;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.domain.biz.BizTaxRisk;
import com.aifc.system.mapper.biz.BizTaxRiskMapper;
import com.aifc.system.service.ai.IAifcAiService;
import com.aifc.system.service.biz.IBizTaxRiskService;

@Service
public class BizTaxRiskServiceImpl implements IBizTaxRiskService
{
    private static final Logger log = LoggerFactory.getLogger(BizTaxRiskServiceImpl.class);
    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);
    private static final String TYPE_LACK_INVOICE = "缺票风险";
    private static final String TYPE_TAX_BURDEN = "税负异常风险";
    private static final String TYPE_INVOICE_ABNORMAL = "发票异常风险";
    private static final String TYPE_PRIVATE_MIX = "公私户混淆风险";
    private static final String TYPE_ZERO_DECLARE = "长期零申报风险";
    private static final String TYPE_INVENTORY = "库存账实不符风险";
    private static final String TYPE_SALARY = "工资异常风险";
    private static final List<String> ALL_TYPES = new ArrayList<>();

    static
    {
        ALL_TYPES.add(TYPE_LACK_INVOICE);
        ALL_TYPES.add(TYPE_TAX_BURDEN);
        ALL_TYPES.add(TYPE_INVOICE_ABNORMAL);
        ALL_TYPES.add(TYPE_PRIVATE_MIX);
        ALL_TYPES.add(TYPE_ZERO_DECLARE);
        ALL_TYPES.add(TYPE_INVENTORY);
        ALL_TYPES.add(TYPE_SALARY);
    }

    @Autowired
    private BizTaxRiskMapper bizTaxRiskMapper;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Autowired
    private IAifcAiService aifcAiService;

    @Override
    public BizTaxRisk selectBizTaxRiskById(Long id)
    {
        BizTaxRisk risk = bizTaxRiskMapper.selectBizTaxRiskById(id);
        if (risk != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(risk.getCompanyId());
        }
        return risk;
    }

    @Override
    public List<BizTaxRisk> selectBizTaxRiskList(BizTaxRisk bizTaxRisk)
    {
        applyCompanyScope(bizTaxRisk);
        return bizTaxRiskMapper.selectBizTaxRiskList(bizTaxRisk);
    }

    @Override
    public int insertBizTaxRisk(BizTaxRisk bizTaxRisk)
    {
        aifcDataScopeService.ensureCompanyAccessible(bizTaxRisk.getCompanyId());
        bizTaxRisk.setCreateTime(DateUtils.getNowDate());
        return bizTaxRiskMapper.insertBizTaxRisk(bizTaxRisk);
    }

    @Override
    public int updateBizTaxRisk(BizTaxRisk bizTaxRisk)
    {
        BizTaxRisk exists = bizTaxRiskMapper.selectBizTaxRiskById(bizTaxRisk.getId());
        if (exists == null)
        {
            throw new ServiceException("risk not found");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        bizTaxRisk.setUpdateTime(DateUtils.getNowDate());
        return bizTaxRiskMapper.updateBizTaxRisk(bizTaxRisk);
    }

    @Override
    public int deleteBizTaxRiskById(Long id)
    {
        BizTaxRisk exists = bizTaxRiskMapper.selectBizTaxRiskById(id);
        if (exists != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        }
        return bizTaxRiskMapper.deleteBizTaxRiskById(id);
    }

    @Override
    public int deleteBizTaxRiskByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                BizTaxRisk exists = bizTaxRiskMapper.selectBizTaxRiskById(id);
                if (exists != null)
                {
                    aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
                }
            }
        }
        return bizTaxRiskMapper.deleteBizTaxRiskByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanRisks(Long companyId, String reportMonth)
    {
        if (companyId == null || StringUtils.isEmpty(reportMonth))
        {
            throw new ServiceException("companyId and reportMonth are required");
        }
        return doScan(companyId, reportMonth, true, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int quickScanRisks(Long companyId, String reportMonth)
    {
        if (companyId == null || StringUtils.isEmpty(reportMonth))
        {
            throw new ServiceException("companyId and reportMonth are required");
        }
        return doScan(companyId, reportMonth, true, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoScanLatestMonthForAllCompanies()
    {
        int total = 0;
        List<Map<String, Object>> rows = bizTaxRiskMapper.selectCompanyLatestMonths();
        for (Map<String, Object> row : rows)
        {
            Long companyId = toLong(row.get("companyId"));
            String reportMonth = row.get("reportMonth") == null ? null : String.valueOf(row.get("reportMonth"));
            if (companyId == null || StringUtils.isEmpty(reportMonth))
            {
                continue;
            }
            total += doScan(companyId, reportMonth, false, false);
        }
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoQuickScanLatestMonthForAllCompanies()
    {
        int total = 0;
        List<Map<String, Object>> rows = bizTaxRiskMapper.selectCompanyLatestMonths();
        for (Map<String, Object> row : rows)
        {
            Long companyId = toLong(row.get("companyId"));
            String reportMonth = row.get("reportMonth") == null ? null : String.valueOf(row.get("reportMonth"));
            if (companyId == null || StringUtils.isEmpty(reportMonth))
            {
                continue;
            }
            total += doScan(companyId, reportMonth, false, true);
        }
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleRisk(Long id, Integer status, String handleRemark)
    {
        if (id == null)
        {
            throw new ServiceException("id is required");
        }
        validateHandleStatus(status);
        BizTaxRisk exists = bizTaxRiskMapper.selectBizTaxRiskById(id);
        if (exists == null)
        {
            throw new ServiceException("risk not found");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        BizTaxRisk risk = new BizTaxRisk();
        risk.setId(id);
        risk.setStatus(status);
        risk.setHandleRemark(handleRemark);
        risk.setUpdateTime(DateUtils.getNowDate());
        return bizTaxRiskMapper.updateBizTaxRisk(risk);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleRiskBatch(Long[] ids, Integer status, String handleRemark)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ServiceException("ids are required");
        }
        validateHandleStatus(status);
        int rows = 0;
        for (Long id : ids)
        {
            rows += handleRisk(id, status, handleRemark);
        }
        return rows;
    }

    private int doScan(Long companyId, String reportMonth, boolean checkPermission, boolean skipAi)
    {
        if (checkPermission)
        {
            aifcDataScopeService.ensureCompanyAccessible(companyId);
        }

        BizTaxRisk query = new BizTaxRisk();
        query.setCompanyId(companyId);
        query.setReportMonth(reportMonth);

        Map<String, Object> base = bizTaxRiskMapper.selectRiskScanBase(query);
        if (base == null || base.isEmpty())
        {
            return 0;
        }

        if (!skipAi)
        {
            int aiRows = doFullAiScan(companyId, reportMonth, query, base);
            if (aiRows >= 0)
            {
                return aiRows;
            }
        }
        return doRuleFallbackScan(companyId, reportMonth, query, base);
    }

    private int doFullAiScan(Long companyId, String reportMonth, BizTaxRisk query, Map<String, Object> base)
    {
        try
        {
            BigDecimal totalIncome = toBigDecimal(base.get("totalIncome"));
            BigDecimal invoiceAmount = toBigDecimal(base.get("invoiceAmount"));
            BigDecimal taxPaid = toBigDecimal(base.get("taxPaid"));
            BigDecimal vatMin = toBigDecimal(base.get("vatMin"));

            int totalInvoice = bizTaxRiskMapper.countInvoiceTotal(query);
            int abnormalInvoice = bizTaxRiskMapper.countInvoiceAbnormal(query);
            int privateMixCount = bizTaxRiskMapper.countVoucherPrivateMix(query);

            YearMonth currentMonth = parseYearMonth(reportMonth);
            query.getParams().put("startMonth", currentMonth.minusMonths(5).format(YM_FMT));
            int zeroMonths = bizTaxRiskMapper.countZeroIncomeMonths(query);

            Map<String, Object> inventoryStats = bizTaxRiskMapper.selectInventoryRiskStats(query);
            BigDecimal maxDiffRate = toBigDecimal(inventoryStats.get("maxDiffRate"));
            BigDecimal totalDiffAmount = toBigDecimal(inventoryStats.get("totalDiffAmount"));

            Map<String, Object> salaryStats = bizTaxRiskMapper.selectSalaryRiskStats(query);
            int totalSalaryCount = toInt(salaryStats.get("totalCount"));
            int is5000Count = toInt(salaryStats.get("is5000Count"));
            int distinctSalaryCount = toInt(salaryStats.get("distinctSalaryCount"));
            BigDecimal salaryTotal = toBigDecimal(salaryStats.get("salaryTotal"));

            String systemPrompt = "你是企业税务风控专家。请基于企业当月经营数据，对以下7类风险逐项判断。只输出JSON，不要输出其他文字。";
            String userPrompt = buildAiDiagnosisPrompt(companyId, reportMonth, base, totalIncome, invoiceAmount, taxPaid, vatMin,
                totalInvoice, abnormalInvoice, privateMixCount, zeroMonths, maxDiffRate, totalDiffAmount, totalSalaryCount,
                is5000Count, distinctSalaryCount, salaryTotal);

            String aiText = aifcAiService.chat(systemPrompt, userPrompt);
            JSONObject json = tryParseJson(aiText);
            if (json == null)
            {
                log.warn("[tax-risk-ai] parse failed, fallback to rule. companyId={}, reportMonth={}", companyId, reportMonth);
                return -1;
            }

            JSONArray risks = json.getJSONArray("risks");
            if (risks == null || risks.isEmpty())
            {
                log.warn("[tax-risk-ai] empty risks, fallback to rule. companyId={}, reportMonth={}", companyId, reportMonth);
                return -1;
            }

            int affected = 0;
            List<String> triggeredTypes = new ArrayList<>();
            for (int i = 0; i < risks.size(); i++)
            {
                JSONObject item = risks.getJSONObject(i);
                if (item == null)
                {
                    continue;
                }
                String type = normalizeRiskType(item.getString("riskType"), item.getString("risk_type"));
                if (StringUtils.isEmpty(type))
                {
                    continue;
                }
                String level = normalizeRiskLevel(firstNotEmpty(item.getString("riskLevel"), item.getString("risk_level")), "low");
                String content = firstNotEmpty(item.getString("riskContent"), item.getString("risk_content"));
                String suggestion = firstNotEmpty(item.getString("aiSuggestion"), item.getString("ai_suggestion"));
                if (StringUtils.isEmpty(content))
                {
                    content = "AI未提供详细风险描述";
                }
                if (StringUtils.isEmpty(suggestion))
                {
                    suggestion = "建议结合业务单据与申报数据复核，必要时由财税人员复审。";
                }
                affected += upsertRisk(companyId, reportMonth, type, level, content, suggestion, null, triggeredTypes);
            }

            if (triggeredTypes.isEmpty())
            {
                return -1;
            }
            clearStaleRisks(companyId, reportMonth, triggeredTypes);
            return affected;
        }
        catch (Exception e)
        {
            log.warn("[tax-risk-ai] full scan failed, fallback to rule. companyId={}, reportMonth={}", companyId, reportMonth, e);
            return -1;
        }
    }

    private String buildAiDiagnosisPrompt(Long companyId, String reportMonth, Map<String, Object> base, BigDecimal totalIncome,
                                          BigDecimal invoiceAmount, BigDecimal taxPaid, BigDecimal vatMin, int totalInvoice,
                                          int abnormalInvoice, int privateMixCount, int zeroMonths, BigDecimal maxDiffRate,
                                          BigDecimal totalDiffAmount, int totalSalaryCount, int is5000Count,
                                          int distinctSalaryCount, BigDecimal salaryTotal)
    {
        String industry = base.get("industry") == null ? "" : String.valueOf(base.get("industry"));
        StringBuilder sb = new StringBuilder();
        sb.append("请按固定7类风险输出JSON。").append('\n');
        sb.append("企业ID=").append(companyId).append('\n');
        sb.append("申报月份=").append(reportMonth).append('\n');
        sb.append("行业=").append(industry).append('\n');
        sb.append("总收入=").append(totalIncome).append('\n');
        sb.append("有票成本=").append(invoiceAmount).append('\n');
        sb.append("已缴税额=").append(taxPaid).append('\n');
        sb.append("行业税负下限=").append(vatMin).append('\n');
        sb.append("发票总数=").append(totalInvoice).append('\n');
        sb.append("异常发票数=").append(abnormalInvoice).append('\n');
        sb.append("公私户混淆凭证数=").append(privateMixCount).append('\n');
        sb.append("近6个月零收入月数=").append(zeroMonths).append('\n');
        sb.append("库存最大差异率(%)=").append(maxDiffRate).append('\n');
        sb.append("库存总差异额=").append(totalDiffAmount).append('\n');
        sb.append("工资记录数=").append(totalSalaryCount).append('\n');
        sb.append("疑似5000档人数=").append(is5000Count).append('\n');
        sb.append("工资档位数=").append(distinctSalaryCount).append('\n');
        sb.append("工资总额=").append(salaryTotal).append('\n');
        sb.append("固定7类风险：缺票风险、税负异常风险、发票异常风险、公私户混淆风险、长期零申报风险、库存账实不符风险、工资异常风险。").append('\n');
        sb.append("输出要求：").append('\n');
        sb.append("1) 必须输出JSON对象，字段为 risks。").append('\n');
        sb.append("2) risks 数组必须包含上述7类风险，每类一条。").append('\n');
        sb.append("3) 每条字段：riskType、riskLevel、riskContent、aiSuggestion。").append('\n');
        sb.append("4) riskLevel 只能是 high/medium/low。").append('\n');
        sb.append("5) riskContent 要结合给定数据。").append('\n');
        sb.append("示例：{\"risks\":[{\"riskType\":\"缺票风险\",\"riskLevel\":\"high\",\"riskContent\":\"...\",\"aiSuggestion\":\"...\"}]}");
        return sb.toString();
    }

    private int doRuleFallbackScan(Long companyId, String reportMonth, BizTaxRisk query, Map<String, Object> base)
    {
        BigDecimal totalIncome = toBigDecimal(base.get("totalIncome"));
        BigDecimal invoiceAmount = toBigDecimal(base.get("invoiceAmount"));
        BigDecimal taxPaid = toBigDecimal(base.get("taxPaid"));
        BigDecimal vatMin = toBigDecimal(base.get("vatMin"));

        int affected = 0;
        List<String> triggeredTypes = new ArrayList<>();

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal invoiceRatio = invoiceAmount.divide(totalIncome, 4, RoundingMode.HALF_UP);
            if (invoiceRatio.compareTo(new BigDecimal("0.60")) < 0)
            {
                String level = invoiceRatio.compareTo(new BigDecimal("0.40")) < 0 ? "high" : "medium";
                affected += upsertRisk(companyId, reportMonth, TYPE_LACK_INVOICE, level,
                    "发票覆盖率(invoice_amount / total_income)=" + invoiceRatio,
                    "补齐合规发票并按月核对成本票据。", null, triggeredTypes);
            }
        }

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0 && vatMin.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal taxRate = taxPaid.divide(totalIncome, 4, RoundingMode.HALF_UP);
            if (taxRate.compareTo(vatMin) < 0)
            {
                affected += upsertRisk(companyId, reportMonth, TYPE_TAX_BURDEN, "high",
                    "实际税负率=" + taxRate + "，低于行业下限 " + vatMin,
                    "复核申报数据并核验税基口径。", null, triggeredTypes);
            }
        }

        int totalInvoice = bizTaxRiskMapper.countInvoiceTotal(query);
        int abnormalInvoice = bizTaxRiskMapper.countInvoiceAbnormal(query);
        if (totalInvoice > 0 && abnormalInvoice > 0)
        {
            BigDecimal abnormalRate = new BigDecimal(abnormalInvoice).divide(new BigDecimal(totalInvoice), 4, RoundingMode.HALF_UP);
            if (abnormalRate.compareTo(new BigDecimal("0.30")) >= 0 || abnormalInvoice >= 3)
            {
                String level = abnormalRate.compareTo(new BigDecimal("0.50")) >= 0 ? "high" : "medium";
                affected += upsertRisk(companyId, reportMonth, TYPE_INVOICE_ABNORMAL, level,
                    "异常发票数=" + abnormalInvoice + "，发票总数=" + totalInvoice,
                    "重点核验连号/整数金额发票真实性与业务一致性。", null, triggeredTypes);
            }
        }

        int privateMixCount = bizTaxRiskMapper.countVoucherPrivateMix(query);
        if (privateMixCount >= 3)
        {
            String level = privateMixCount >= 8 ? "high" : "medium";
            affected += upsertRisk(companyId, reportMonth, TYPE_PRIVATE_MIX, level,
                "命中公私混淆关键词凭证数=" + privateMixCount,
                "规范对公收支，减少私户往来并清理股东借款。", null, triggeredTypes);
        }

        YearMonth currentMonth = parseYearMonth(reportMonth);
        query.getParams().put("startMonth", currentMonth.minusMonths(5).format(YM_FMT));
        int zeroMonths = bizTaxRiskMapper.countZeroIncomeMonths(query);
        if (zeroMonths >= 6)
        {
            affected += upsertRisk(companyId, reportMonth, TYPE_ZERO_DECLARE, "high",
                "近6个月零收入月份数=" + zeroMonths,
                "核实经营真实性，避免长期连续零申报。", null, triggeredTypes);
        }

        Map<String, Object> inventoryStats = bizTaxRiskMapper.selectInventoryRiskStats(query);
        BigDecimal maxDiffRate = toBigDecimal(inventoryStats.get("maxDiffRate"));
        if (maxDiffRate.compareTo(new BigDecimal("10")) > 0)
        {
            String level = maxDiffRate.compareTo(new BigDecimal("20")) > 0 ? "high" : "medium";
            affected += upsertRisk(companyId, reportMonth, TYPE_INVENTORY, level,
                "库存最大差异率=" + maxDiffRate + "%",
                "执行盘点对账并及时调整账实差异。", null, triggeredTypes);
        }

        Map<String, Object> salaryStats = bizTaxRiskMapper.selectSalaryRiskStats(query);
        int totalSalaryCount = toInt(salaryStats.get("totalCount"));
        int is5000Count = toInt(salaryStats.get("is5000Count"));
        int distinctSalaryCount = toInt(salaryStats.get("distinctSalaryCount"));
        if (totalSalaryCount >= 5)
        {
            BigDecimal ratio5000 = new BigDecimal(is5000Count).divide(new BigDecimal(totalSalaryCount), 4, RoundingMode.HALF_UP);
            if (ratio5000.compareTo(new BigDecimal("0.70")) >= 0 || distinctSalaryCount <= 1)
            {
                String level = ratio5000.compareTo(new BigDecimal("0.85")) >= 0 ? "high" : "medium";
                affected += upsertRisk(companyId, reportMonth, TYPE_SALARY, level,
                    "工资记录数=" + totalSalaryCount + "，疑似5000占比=" + ratio5000 + "，工资档位数=" + distinctSalaryCount,
                    "核查薪资结构真实性并完善个税留痕。", null, triggeredTypes);
            }
        }

        clearStaleRisks(companyId, reportMonth, triggeredTypes);
        return affected;
    }

    private int upsertRisk(Long companyId, String reportMonth, String type, String level, String content, String suggestion,
                           String relatedIds, List<String> triggeredTypes)
    {
        if (!triggeredTypes.contains(type))
        {
            triggeredTypes.add(type);
        }

        BizTaxRisk key = new BizTaxRisk();
        key.setCompanyId(companyId);
        key.setReportMonth(reportMonth);
        key.setRiskType(type);
        BizTaxRisk exists = bizTaxRiskMapper.selectByCompanyMonthAndType(key);

        BizTaxRisk risk = new BizTaxRisk();
        risk.setRiskLevel(normalizeRiskLevel(level, "low"));
        risk.setRiskContent(content);
        risk.setAiSuggestion(suggestion);
        risk.setRelatedIds(relatedIds);

        if (exists != null)
        {
            risk.setId(exists.getId());
            risk.setUpdateTime(DateUtils.getNowDate());
            return bizTaxRiskMapper.updateBizTaxRisk(risk);
        }

        risk.setCompanyId(companyId);
        risk.setReportMonth(reportMonth);
        risk.setRiskType(type);
        risk.setStatus(0);
        risk.setHandleRemark(null);
        risk.setCreateTime(DateUtils.getNowDate());
        return bizTaxRiskMapper.insertBizTaxRisk(risk);
    }

    private void clearStaleRisks(Long companyId, String reportMonth, List<String> triggeredTypes)
    {
        BizTaxRisk q = new BizTaxRisk();
        q.setCompanyId(companyId);
        q.setReportMonth(reportMonth);
        q.getParams().put("riskTypes", triggeredTypes);
        bizTaxRiskMapper.deleteByCompanyMonthAndTypeNotIn(q);
    }

    private JSONObject tryParseJson(String aiText)
    {
        if (StringUtils.isEmpty(aiText))
        {
            return null;
        }
        String text = aiText.trim();
        try
        {
            return JSONObject.parseObject(text);
        }
        catch (Exception ignore)
        {
        }

        Matcher m = JSON_BLOCK_PATTERN.matcher(text);
        if (m.find())
        {
            try
            {
                return JSONObject.parseObject(m.group(1));
            }
            catch (Exception ignore)
            {
            }
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            try
            {
                return JSONObject.parseObject(text.substring(start, end + 1));
            }
            catch (Exception ignore)
            {
            }
        }
        return null;
    }

    private String normalizeRiskType(String v1, String v2)
    {
        String raw = firstNotEmpty(v1, v2);
        if (StringUtils.isEmpty(raw))
        {
            return null;
        }
        String text = raw.replace(" ", "").trim();
        if (text.contains("缺票"))
        {
            return TYPE_LACK_INVOICE;
        }
        if (text.contains("税负"))
        {
            return TYPE_TAX_BURDEN;
        }
        if (text.contains("发票") && text.contains("异常"))
        {
            return TYPE_INVOICE_ABNORMAL;
        }
        if (text.contains("公私"))
        {
            return TYPE_PRIVATE_MIX;
        }
        if (text.contains("零申报"))
        {
            return TYPE_ZERO_DECLARE;
        }
        if (text.contains("库存"))
        {
            return TYPE_INVENTORY;
        }
        if (text.contains("工资"))
        {
            return TYPE_SALARY;
        }
        for (String item : ALL_TYPES)
        {
            if (item.equals(text))
            {
                return item;
            }
        }
        return null;
    }

    private String normalizeRiskLevel(String aiLevel, String defaultLevel)
    {
        if (StringUtils.isEmpty(aiLevel))
        {
            return defaultLevel;
        }
        String v = aiLevel.trim().toLowerCase();
        if ("high".equals(v) || v.contains("高"))
        {
            return "high";
        }
        if ("medium".equals(v) || "mid".equals(v) || v.contains("中"))
        {
            return "medium";
        }
        if ("low".equals(v) || v.contains("低"))
        {
            return "low";
        }
        return defaultLevel;
    }

    private String firstNotEmpty(String a, String b)
    {
        if (StringUtils.isNotEmpty(a))
        {
            return a;
        }
        return b;
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

    private int toInt(Object value)
    {
        if (value == null)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private YearMonth parseYearMonth(String reportMonth)
    {
        try
        {
            return YearMonth.parse(reportMonth, YM_FMT);
        }
        catch (Exception e)
        {
            throw new ServiceException("reportMonth format should be yyyy-MM");
        }
    }

    private void validateHandleStatus(Integer status)
    {
        if (status == null || (status != 0 && status != 1 && status != 2))
        {
            throw new ServiceException("status must be 0(unhandled) or 1(processed) or 2(ignored)");
        }
    }

    private void applyCompanyScope(BizTaxRisk query)
    {
        if (query == null)
        {
            return;
        }
        if (query.getCompanyId() != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(query.getCompanyId());
            return;
        }
        List<Long> companyIds = aifcDataScopeService.getAccessibleCompanyIds();
        if (companyIds != null && !companyIds.isEmpty())
        {
            query.getParams().put("allowedCompanyIds", companyIds);
        }
    }
}
