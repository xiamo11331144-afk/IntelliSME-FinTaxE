package com.aifc.system.service.biz.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import com.alibaba.fastjson2.JSONObject;
import com.aifc.common.config.RuoYiConfig;
import com.aifc.common.constant.Constants;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.DateUtils;
import com.aifc.common.utils.StringUtils;
import com.aifc.common.utils.file.AliyunOssUtils;
import com.aifc.system.domain.biz.BizInvoice;
import com.aifc.system.mapper.biz.BizInvoiceMapper;
import com.aifc.system.service.ai.IAifcAiService;
import com.aifc.system.service.biz.IBizInvoiceService;
import com.aifc.system.utils.PdfUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BizInvoiceServiceImpl implements IBizInvoiceService
{
    private static final Logger log = LoggerFactory.getLogger(BizInvoiceServiceImpl.class);

    @Autowired
    private BizInvoiceMapper bizInvoiceMapper;

    @Autowired
    private IAifcAiService aifcAiService;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Autowired(required = false)
    private AliyunOssUtils aliyunOssUtils;

    @Override
    public BizInvoice selectBizInvoiceById(Long id)
    {
        BizInvoice invoice = bizInvoiceMapper.selectBizInvoiceById(id);
        if (invoice != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(invoice.getCompanyId());
        }
        return invoice;
    }

    @Override
    public List<BizInvoice> selectBizInvoiceList(BizInvoice bizInvoice)
    {
        applyCompanyScope(bizInvoice);
        return bizInvoiceMapper.selectBizInvoiceList(bizInvoice);
    }

    @Override
    public int insertBizInvoice(BizInvoice bizInvoice)
    {
        aifcDataScopeService.ensureCompanyAccessible(bizInvoice.getCompanyId());
        if (bizInvoice.getAmount() == null || bizInvoice.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("发票金额不能为空且必须大于0");
        }
        if (StringUtils.isEmpty(bizInvoice.getInvoiceCode()) && StringUtils.isEmpty(bizInvoice.getInvoiceNo()))
        {
            throw new ServiceException("发票代码或发票号码至少填写一个");
        }
        if (bizInvoice.getInvoiceDate() == null)
        {
            bizInvoice.setInvoiceDate(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(bizInvoice.getReportMonth()))
        {
            bizInvoice.setReportMonth(new SimpleDateFormat("yyyy-MM").format(bizInvoice.getInvoiceDate()));
        }
        if (StringUtils.isEmpty(bizInvoice.getFileType()))
        {
            bizInvoice.setFileType(resolveFileType(null, bizInvoice.getFileUrl()));
        }
        if (bizInvoice.getStatus() == null)
        {
            bizInvoice.setStatus(1);
        }
        if (bizInvoice.getIsInteger() == null)
        {
            bizInvoice.setIsInteger(bizInvoice.getAmount().stripTrailingZeros().scale() <= 0 ? 1 : 0);
        }
        if (bizInvoice.getIsSeries() == null)
        {
            bizInvoice.setIsSeries(0);
        }
        if (StringUtils.isNotEmpty(bizInvoice.getInvoiceCode()) && StringUtils.isNotEmpty(bizInvoice.getInvoiceNo())
            && bizInvoiceMapper.countInvoiceDuplicate(bizInvoice) > 0)
        {
            throw new ServiceException("发票代码+发票号码已存在，请勿重复录入");
        }
        bizInvoice.setCreateTime(DateUtils.getNowDate());
        return bizInvoiceMapper.insertBizInvoice(bizInvoice);
    }

    @Override
    public int updateBizInvoice(BizInvoice bizInvoice)
    {
        BizInvoice exists = bizInvoiceMapper.selectBizInvoiceById(bizInvoice.getId());
        if (exists == null)
        {
            throw new ServiceException("票据不存在");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        bizInvoice.setUpdateTime(DateUtils.getNowDate());
        return bizInvoiceMapper.updateBizInvoice(bizInvoice);
    }

    @Override
    public int deleteBizInvoiceById(Long id)
    {
        BizInvoice exists = bizInvoiceMapper.selectBizInvoiceById(id);
        if (exists != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        }
        return bizInvoiceMapper.deleteBizInvoiceById(id);
    }

    @Override
    public int deleteBizInvoiceByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                BizInvoice exists = bizInvoiceMapper.selectBizInvoiceById(id);
                if (exists != null)
                {
                    aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
                }
            }
        }
        return bizInvoiceMapper.deleteBizInvoiceByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInvoice recognizeAndCreateDraft(Long companyId, String fileUrl, String fileType, String rawText)
    {
        if (companyId == null)
        {
            throw new ServiceException("companyId is required");
        }
        if (StringUtils.isEmpty(fileUrl) && StringUtils.isEmpty(rawText))
        {
            throw new ServiceException("fileUrl or rawText is required");
        }

        trace(String.format("[invoice-recognize] start companyId=%s, fileType=%s, hasFileUrl=%s, rawTextLen=%s",
            companyId, fileType, StringUtils.isNotEmpty(fileUrl), rawText == null ? 0 : rawText.length()));

        aifcDataScopeService.ensureCompanyAccessible(companyId);
        BizInvoice invoice = buildDraft(companyId, fileUrl, fileType, rawText);
        validateDraft(invoice);
        markInvoiceFlags(invoice);

        trace(String.format("[invoice-recognize] final draft companyId=%s, code=%s, no=%s, amount=%s, taxAmount=%s, date=%s, seller=%s, buyer=%s, goods=%s, reportMonth=%s, isInteger=%s, isSeries=%s",
            invoice.getCompanyId(),
            invoice.getInvoiceCode(),
            invoice.getInvoiceNo(),
            invoice.getAmount(),
            invoice.getTaxAmount(),
            invoice.getInvoiceDate(),
            shortText(invoice.getSellerName(), 30),
            shortText(invoice.getBuyerName(), 30),
            shortText(invoice.getGoodsName(), 30),
            invoice.getReportMonth(),
            invoice.getIsInteger(),
            invoice.getIsSeries()));

        if (StringUtils.isNotEmpty(invoice.getInvoiceCode()) && StringUtils.isNotEmpty(invoice.getInvoiceNo())
            && bizInvoiceMapper.countInvoiceDuplicate(invoice) > 0)
        {
            throw new ServiceException("发票代码+发票号码已存在，请勿重复录入");
        }

        invoice.setCreateTime(DateUtils.getNowDate());
        bizInvoiceMapper.insertBizInvoice(invoice);
        trace("[invoice-recognize] inserted id=" + invoice.getId());
        return invoice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmAndCreateVoucher(Long id, String subject, Integer entryType, String entryRemark)
    {
        BizInvoice invoice = bizInvoiceMapper.selectBizInvoiceById(id);
        if (invoice == null)
        {
            throw new ServiceException("票据不存在");
        }
        aifcDataScopeService.ensureCompanyAccessible(invoice.getCompanyId());
        if (bizInvoiceMapper.countVoucherByInvoiceId(id) > 0)
        {
            throw new ServiceException("该票据已生成凭证，不能重复入账");
        }
        if (StringUtils.isEmpty(subject))
        {
            subject = "Management Expense";
        }
        if (entryType == null || (entryType != 1 && entryType != 2))
        {
            entryType = 2;
        }
        if (invoice.getInvoiceDate() == null)
        {
            invoice.setInvoiceDate(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(invoice.getReportMonth()))
        {
            invoice.setReportMonth(new SimpleDateFormat("yyyy-MM").format(invoice.getInvoiceDate()));
        }
        if (invoice.getAmount() == null)
        {
            invoice.setAmount(BigDecimal.ZERO);
        }
        invoice.getParams().put("subject", subject);
        invoice.getParams().put("entryType", entryType);
        invoice.getParams().put("entryRemark", StringUtils.isEmpty(entryRemark) ? "Auto entry from invoice confirmation" : entryRemark);
        return bizInvoiceMapper.insertBizVoucherByInvoice(invoice);
    }

    // remaining methods keep same behavior
    private BizInvoice buildDraft(Long companyId, String fileUrl, String fileType, String rawText)
    {
        String effectiveRawText = rawText;
        if (StringUtils.isEmpty(effectiveRawText))
        {
            boolean remoteHttp = StringUtils.isNotEmpty(fileUrl)
                && isHttpUrl(fileUrl)
                && !fileUrl.contains("localhost")
                && !fileUrl.contains("127.0.0.1");
            if (!remoteHttp)
            {
                effectiveRawText = enrichRawTextFromFile(rawText, fileUrl, fileType);
            }
        }

        BizInvoice invoice = new BizInvoice();
        invoice.setCompanyId(companyId);
        invoice.setFileUrl(fileUrl);
        invoice.setFileType(resolveFileType(fileType, fileUrl));
        invoice.setStatus(1);
        invoice.setIsInteger(0);
        invoice.setIsSeries(0);

        BizInvoice byRegex = buildDraftByRegex(effectiveRawText);
        logStage("regex", byRegex);
        mergeInvoice(invoice, byRegex);

        String userPrompt = buildPrompt(effectiveRawText, fileUrl, fileType);
        String aiText;
        if (StringUtils.isNotEmpty(fileUrl) && StringUtils.isEmpty(effectiveRawText) && isHttpUrl(fileUrl))
        {
            String visionImageUrl = prepareVisionImageUrl(fileUrl, fileType);
            if (fileUrl.contains("localhost") || fileUrl.contains("127.0.0.1"))
            {
                trace("[invoice-recognize][ai] warning: imageUrl is local address, external AI provider may not access it");
            }
            trace("[invoice-recognize][ai] use vision model with imageUrl=" + visionImageUrl);
            aiText = aifcAiService.chatWithImage(
                "You are a finance invoice extraction assistant. Return JSON only. Date format: yyyy-MM-dd.",
                "Extract invoice fields and return JSON only: "
                    + "{\"invoiceCode\":\"\",\"invoiceNo\":\"\",\"amount\":\"\",\"taxAmount\":\"\",\"invoiceDate\":\"\",\"sellerName\":\"\",\"buyerName\":\"\",\"goodsName\":\"\"}. If unknown, use empty string.",
                visionImageUrl);
        }
        else
        {
            aiText = aifcAiService.chat("You are a finance invoice extraction assistant. Return JSON only. Date format: yyyy-MM-dd.", userPrompt);
        }
        trace("[invoice-recognize][ai] raw=" + shortText(aiText, 500));

        JSONObject json = tryParseJson(aiText);
        if (json == null && StringUtils.isEmpty(effectiveRawText)
            && (aiText != null && (aiText.contains("disabled") || aiText.contains("missing") || aiText.contains("failed"))))
        {
            throw new ServiceException("AI识别不可用，请先配置aifc.ai参数，或手动粘贴OCR文本再识别");
        }
        if (json != null)
        {
            BizInvoice byAi = new BizInvoice();
            byAi.setInvoiceCode(json.getString("invoiceCode"));
            byAi.setInvoiceNo(json.getString("invoiceNo"));
            byAi.setSellerName(json.getString("sellerName"));
            byAi.setBuyerName(json.getString("buyerName"));
            byAi.setGoodsName(json.getString("goodsName"));
            byAi.setAmount(parseDecimal(json.getString("amount")));
            byAi.setTaxAmount(parseDecimal(json.getString("taxAmount")));
            byAi.setInvoiceDate(parseDate(json.getString("invoiceDate")));
            logStage("ai-json", byAi);
            mergeInvoice(invoice, byAi);
        }
        else
        {
            trace("[invoice-recognize][ai] parse json failed");
        }

        fillReportMonth(invoice);
        return invoice;
    }

    private String enrichRawTextFromFile(String rawText, String fileUrl, String fileType)
    {
        if (StringUtils.isNotEmpty(rawText))
        {
            return rawText;
        }
        if (StringUtils.isEmpty(fileUrl))
        {
            return rawText;
        }

        String detectedType = resolveFileType(fileType, fileUrl);
        String localPath = toLocalPath(fileUrl);
        trace("[invoice-recognize][file] detectedType=" + detectedType + ", localPath=" + localPath);

        if (StringUtils.isEmpty(localPath))
        {
            return rawText;
        }

        try
        {
            if ("pdf".equalsIgnoreCase(detectedType))
            {
                String text = PdfUtil.pdfToString(localPath);
                trace("[invoice-recognize][file] pdf textLen=" + (text == null ? 0 : text.length()));
                String cleaned = text == null ? "" : text.replaceAll("\\s+", "");
                trace("[invoice-recognize][file] pdf cleanedTextLen=" + cleaned.length());
                if (StringUtils.isEmpty(cleaned) || cleaned.length() < 10)
                {
                    String ocrText = PdfUtil.pdfToStringByOcr(localPath);
                    String ocrCleaned = ocrText == null ? "" : ocrText.replaceAll("\\s+", "");
                    trace("[invoice-recognize][file] pdf ocrTextLen=" + (ocrText == null ? 0 : ocrText.length()));
                    trace("[invoice-recognize][file] pdf ocrCleanedTextLen=" + ocrCleaned.length());
                    if (StringUtils.isNotEmpty(ocrCleaned) && ocrCleaned.length() >= 10)
                    {
                        return ocrText;
                    }
                throw new ServiceException("PDF未提取到有效文本，且OCR失败。请确认已安装Tesseract并包含chi_sim语言包，或手动粘贴OCR文本");
                }
                return text;
            }
            if ("txt".equalsIgnoreCase(detectedType))
            {
                String text = readTxt(localPath);
                trace("[invoice-recognize][file] txt textLen=" + (text == null ? 0 : text.length()));
                return StringUtils.isNotEmpty(text) ? text : rawText;
            }
            trace("[invoice-recognize][file] unsupported fileType for local extraction: " + detectedType);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            trace("[invoice-recognize][file] extract failed: " + e.getMessage());
        }
        return rawText;
    }

    private String toLocalPath(String fileUrl)
    {
        String url = fileUrl;
        int idxHttpProfile = url.indexOf(Constants.RESOURCE_PREFIX);
        if (idxHttpProfile >= 0)
        {
            url = url.substring(idxHttpProfile);
        }
        if (!url.startsWith(Constants.RESOURCE_PREFIX))
        {
            return null;
        }
        String relative = StringUtils.substringAfter(url, Constants.RESOURCE_PREFIX);
        return RuoYiConfig.getProfile() + relative;
    }

    private String readTxt(String localPath) throws Exception
    {
        if (!Files.exists(Paths.get(localPath)))
        {
            return null;
        }
        byte[] bytes = Files.readAllBytes(Paths.get(localPath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private BizInvoice buildDraftByRegex(String rawText)
    {
        BizInvoice invoice = new BizInvoice();
        if (StringUtils.isEmpty(rawText))
        {
            return invoice;
        }
        String normalizedText = normalizeRegexText(rawText);

        String invoiceCodeLabel = "发票代码";
        String invoiceNoLabel = "发票号码";
        String sellerLabel1 = "销售方名称";
        String sellerLabel2 = "销方名称";
        String buyerLabel1 = "购买方名称";
        String buyerLabel2 = "购方名称";
        String goodsLabel1 = "货物或应税劳务、服务名称";
        String goodsLabel2 = "货物名称";
        String goodsLabel3 = "服务名称";
        String amountLabel1 = "价税合计";
        String amountLabel2 = "金额合计";
        String amountLabel3 = "合计金额";
        String amountLabel4 = "小写";
        String amountLabel5 = "合计";
        String amountLabel6 = "金额";
        String taxLabel = "税额";
        String dateLabel1 = "开票日期";
        String dateLabel2 = "日期";

        invoice.setInvoiceCode(matchFirst(normalizedText, "(?i)(" + invoiceCodeLabel + "|invoice\\s*code)(?:\\s|:)*([0-9A-Za-z]{8,20})", 2));
        invoice.setInvoiceNo(matchFirst(normalizedText, "(?i)(" + invoiceNoLabel + "|invoice\\s*no)(?:\\s|:)*([0-9A-Za-z]{6,20})", 2));
        invoice.setSellerName(matchFirst(normalizedText, "(" + sellerLabel1 + "|" + sellerLabel2 + ")(?:\\s|:)*([^\\n\\r]{2,80})", 2));
        invoice.setBuyerName(matchFirst(normalizedText, "(" + buyerLabel1 + "|" + buyerLabel2 + ")(?:\\s|:)*([^\\n\\r]{2,80})", 2));
        invoice.setGoodsName(matchFirst(normalizedText, "(" + goodsLabel1 + "|" + goodsLabel2 + "|" + goodsLabel3 + ")(?:\\s|:)*([^\\n\\r]{2,120})", 2));
        invoice.setAmount(parseDecimal(matchFirst(normalizedText, "(" + amountLabel1 + "|" + amountLabel2 + "|" + amountLabel3 + "|" + amountLabel4 + "|" + amountLabel5 + "|" + amountLabel6 + ")(?:\\s|:|\\(|\\))*((?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d{1,2})?)", 2)));
        invoice.setTaxAmount(parseDecimal(matchFirst(normalizedText, "(" + taxLabel + ")(?:\\s|:)*((?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d{1,2})?)", 2)));
        invoice.setInvoiceDate(parseDate(matchFirst(normalizedText, "(" + dateLabel1 + "|" + dateLabel2 + ")(?:\\s|:)*(\\d{4}(?:-|\\.|/)\\d{1,2}(?:-|\\.|/)\\d{1,2})", 2)));

        if (invoice.getAmount() == null || invoice.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            invoice.setAmount(extractMaxAmount(normalizedText));
        }
        return invoice;
    }

    private void validateDraft(BizInvoice invoice)
    {
        if (invoice.getAmount() == null || invoice.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            trace(String.format("[invoice-recognize] validate failed: amount empty, code=%s, no=%s, seller=%s, buyer=%s, goods=%s",
                invoice.getInvoiceCode(),
                invoice.getInvoiceNo(),
                shortText(invoice.getSellerName(), 30),
                shortText(invoice.getBuyerName(), 30),
                shortText(invoice.getGoodsName(), 30)));
            throw new ServiceException("识别失败：金额为空");
        }
        if (invoice.getInvoiceDate() == null)
        {
            invoice.setInvoiceDate(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(invoice.getInvoiceNo()) && StringUtils.isEmpty(invoice.getInvoiceCode()))
        {
            trace("[invoice-recognize] validate failed: code/no both empty, amount=" + invoice.getAmount());
            throw new ServiceException("识别失败：发票代码和发票号码至少识别出一个");
        }
    }

    private void markInvoiceFlags(BizInvoice invoice)
    {
        if (invoice.getAmount() != null)
        {
            invoice.setIsInteger(invoice.getAmount().stripTrailingZeros().scale() <= 0 ? 1 : 0);
        }
        if (StringUtils.isNotEmpty(invoice.getInvoiceNo()) && StringUtils.isNotEmpty(invoice.getReportMonth()))
        {
            String prevNo = stepInvoiceNo(invoice.getInvoiceNo(), -1);
            String nextNo = stepInvoiceNo(invoice.getInvoiceNo(), 1);
            if (prevNo != null && nextNo != null)
            {
                invoice.getParams().put("prevNo", prevNo);
                invoice.getParams().put("nextNo", nextNo);
                invoice.setIsSeries(bizInvoiceMapper.countSeriesNeighbor(invoice) > 0 ? 1 : 0);
            }
        }
    }

    private void mergeInvoice(BizInvoice target, BizInvoice source)
    {
        if (source == null)
        {
            return;
        }
        if (StringUtils.isEmpty(target.getInvoiceCode()) && StringUtils.isNotEmpty(source.getInvoiceCode()))
        {
            target.setInvoiceCode(source.getInvoiceCode());
        }
        if (StringUtils.isEmpty(target.getInvoiceNo()) && StringUtils.isNotEmpty(source.getInvoiceNo()))
        {
            target.setInvoiceNo(source.getInvoiceNo());
        }
        if (StringUtils.isEmpty(target.getSellerName()) && StringUtils.isNotEmpty(source.getSellerName()))
        {
            target.setSellerName(source.getSellerName());
        }
        if (StringUtils.isEmpty(target.getBuyerName()) && StringUtils.isNotEmpty(source.getBuyerName()))
        {
            target.setBuyerName(source.getBuyerName());
        }
        if (StringUtils.isEmpty(target.getGoodsName()) && StringUtils.isNotEmpty(source.getGoodsName()))
        {
            target.setGoodsName(source.getGoodsName());
        }
        if ((target.getAmount() == null || target.getAmount().compareTo(BigDecimal.ZERO) <= 0) && source.getAmount() != null)
        {
            target.setAmount(source.getAmount());
        }
        if ((target.getTaxAmount() == null || target.getTaxAmount().compareTo(BigDecimal.ZERO) <= 0) && source.getTaxAmount() != null)
        {
            target.setTaxAmount(source.getTaxAmount());
        }
        if (target.getInvoiceDate() == null && source.getInvoiceDate() != null)
        {
            target.setInvoiceDate(source.getInvoiceDate());
        }
    }

    private void fillReportMonth(BizInvoice invoice)
    {
        Date invoiceDate = invoice.getInvoiceDate();
        if (invoiceDate != null)
        {
            invoice.setReportMonth(new SimpleDateFormat("yyyy-MM").format(invoiceDate));
        }
        else
        {
            invoice.setReportMonth(DateUtils.dateTimeNow("yyyy-MM"));
        }
    }

    private String buildPrompt(String rawText, String fileUrl, String fileType)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Extract fields from invoice content and return JSON: ");
        sb.append("{\"invoiceCode\":\"\",\"invoiceNo\":\"\",\"amount\":\"\",\"taxAmount\":\"\",\"invoiceDate\":\"\",\"sellerName\":\"\",\"buyerName\":\"\",\"goodsName\":\"\"}");
        if (StringUtils.isNotEmpty(rawText))
        {
            sb.append(" OCR text: ").append(rawText);
        }
        else
        {
            sb.append(" No OCR text. fileUrl: ").append(fileUrl).append(", fileType: ").append(fileType);
        }
        return sb.toString();
    }

    private String resolveFileType(String fileType, String fileUrl)
    {
        if (StringUtils.isNotEmpty(fileType))
        {
            return fileType.toLowerCase();
        }
        if (StringUtils.isNotEmpty(fileUrl) && fileUrl.contains("."))
        {
            String ext = fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
            return ext.toLowerCase();
        }
        return null;
    }

    private String prepareVisionImageUrl(String fileUrl, String fileType)
    {
        String detectedType = resolveFileType(fileType, fileUrl);
        if (!"pdf".equalsIgnoreCase(detectedType))
        {
            return fileUrl;
        }
        try
        {
            String imageUrl = convertRemotePdfToImageUrl(fileUrl);
            return StringUtils.isNotEmpty(imageUrl) ? imageUrl : fileUrl;
        }
        catch (Exception e)
        {
            trace("[invoice-recognize][ai] convert pdf to image failed: " + e.getMessage());
            return fileUrl;
        }
    }

    private String convertRemotePdfToImageUrl(String fileUrl) throws Exception
    {
        if (aliyunOssUtils == null || !aliyunOssUtils.enabled())
        {
            trace("[invoice-recognize][ai] oss not enabled, skip remote pdf conversion");
            return null;
        }
        byte[] pdfBytes;
        try (InputStream in = new URL(fileUrl).openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1)
            {
                baos.write(buffer, 0, len);
            }
            pdfBytes = baos.toByteArray();
        }
        if (pdfBytes == null || pdfBytes.length == 0)
        {
            return null;
        }
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream imageOut = new ByteArrayOutputStream())
        {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage firstPage = renderer.renderImageWithDPI(0, 200, ImageType.RGB);
            ImageIO.write(firstPage, "png", imageOut);
            byte[] imageBytes = imageOut.toByteArray();
            String uploaded = aliyunOssUtils.uploadBytes("invoice-preview.png", imageBytes);
            trace("[invoice-recognize][ai] converted pdf first page to image: " + uploaded);
            return uploaded;
        }
    }

    private boolean isHttpUrl(String url)
    {
        if (StringUtils.isEmpty(url))
        {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private String normalizeRegexText(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return value;
        }
        return value
            .replace('：', ':')
            .replace('（', '(')
            .replace('）', ')')
            .replace('￥', ' ')
            .replace('¥', ' ')
            .replace('年', '-')
            .replace('月', '-')
            .replace("日", "")
            .replace('\u00A0', ' ');
    }

    private Date parseDate(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        String normalized = value
            .replace("年", "-")
            .replace("月", "-")
            .replace("日", "")
            .replace("/", "-")
            .replace(".", "-")
            .trim();
        return DateUtils.parseDate(normalized);
    }

    private String matchFirst(String text, String regex)
    {
        return matchFirst(text, regex, 1);
    }

    private String matchFirst(String text, String regex, int group)
    {
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        try
        {
            Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
            if (matcher.find())
            {
                return matcher.group(group) == null ? null : matcher.group(group).trim();
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    private String stepInvoiceNo(String invoiceNo, int step)
    {
        if (StringUtils.isEmpty(invoiceNo) || !invoiceNo.matches("\\d+"))
        {
            return null;
        }
        try
        {
            long current = Long.parseLong(invoiceNo);
            long next = current + step;
            if (next < 0)
            {
                return null;
            }
            return String.format("%0" + invoiceNo.length() + "d", next);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private JSONObject tryParseJson(String aiText)
    {
        if (StringUtils.isEmpty(aiText))
        {
            return null;
        }
        String content = aiText.trim();
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start < 0 || end <= start)
        {
            return null;
        }
        String jsonText = content.substring(start, end + 1);
        try
        {
            return JSONObject.parseObject(jsonText);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return BigDecimal.ZERO;
        }
        try
        {
            String normalized = value
                .replace(",", "")
                .replace("￥", "")
                .replace("¥", "")
                .trim();
            return new BigDecimal(normalized);
        }
        catch (Exception e)
        {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal extractMaxAmount(String rawText)
    {
        if (StringUtils.isEmpty(rawText))
        {
            return BigDecimal.ZERO;
        }
        BigDecimal max = BigDecimal.ZERO;
        Pattern pattern = Pattern.compile("(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)");
        Matcher matcher = pattern.matcher(rawText);
        while (matcher.find())
        {
            BigDecimal val = parseDecimal(matcher.group(1));
            if (val != null && val.compareTo(max) > 0)
            {
                max = val;
            }
        }
        return max;
    }

    private void logStage(String stage, BizInvoice invoice)
    {
        if (invoice == null)
        {
            trace("[invoice-recognize][" + stage + "] null");
            return;
        }
        trace(String.format("[invoice-recognize][%s] code=%s, no=%s, amount=%s, taxAmount=%s, date=%s, seller=%s, buyer=%s, goods=%s",
            stage,
            invoice.getInvoiceCode(),
            invoice.getInvoiceNo(),
            invoice.getAmount(),
            invoice.getTaxAmount(),
            invoice.getInvoiceDate(),
            shortText(invoice.getSellerName(), 30),
            shortText(invoice.getBuyerName(), 30),
            shortText(invoice.getGoodsName(), 30)));
    }

    private String shortText(String value, int maxLen)
    {
        if (StringUtils.isEmpty(value))
        {
            return value;
        }
        if (value.length() <= maxLen)
        {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }

    private void trace(String message)
    {
        log.info(message);
        System.out.println(message);
    }

    private void applyCompanyScope(BizInvoice query)
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
