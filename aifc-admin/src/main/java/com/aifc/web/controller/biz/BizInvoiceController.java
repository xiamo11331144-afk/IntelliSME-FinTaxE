package com.aifc.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aifc.common.annotation.Log;
import com.aifc.common.core.controller.BaseController;
import com.aifc.common.core.domain.AjaxResult;
import com.aifc.common.core.page.TableDataInfo;
import com.aifc.common.enums.BusinessType;
import com.aifc.system.domain.biz.BizInvoice;
import com.aifc.system.service.biz.IBizInvoiceService;

@RestController
@RequestMapping("/aifc/invoice")
public class BizInvoiceController extends BaseController
{
    @Autowired
    private IBizInvoiceService bizInvoiceService;

    @PreAuthorize("@ss.hasPermi('aifc:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizInvoice bizInvoice)
    {
        startPage();
        List<BizInvoice> list = bizInvoiceService.selectBizInvoiceList(bizInvoice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bizInvoiceService.selectBizInvoiceById(id));
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:add')")
    @Log(title = "AIFC Invoice", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizInvoice bizInvoice)
    {
        return toAjax(bizInvoiceService.insertBizInvoice(bizInvoice));
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:add')")
    @Log(title = "AIFC Invoice", businessType = BusinessType.INSERT)
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestBody RecognizeRequest request)
    {
        BizInvoice invoice = bizInvoiceService.recognizeAndCreateDraft(
            request.getCompanyId(),
            request.getFileUrl(),
            request.getFileType(),
            request.getRawText());
        return success(invoice);
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:edit')")
    @Log(title = "AIFC Invoice", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/confirm")
    public AjaxResult confirm(@PathVariable Long id, @RequestBody ConfirmRequest request)
    {
        return toAjax(bizInvoiceService.confirmAndCreateVoucher(
            id,
            request.getSubject(),
            request.getEntryType(),
            request.getEntryRemark()));
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:edit')")
    @Log(title = "AIFC Invoice", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizInvoice bizInvoice)
    {
        return toAjax(bizInvoiceService.updateBizInvoice(bizInvoice));
    }

    @PreAuthorize("@ss.hasPermi('aifc:invoice:remove')")
    @Log(title = "AIFC Invoice", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizInvoiceService.deleteBizInvoiceByIds(ids));
    }

    public static class RecognizeRequest
    {
        private Long companyId;
        private String fileUrl;
        private String fileType;
        private String rawText;

        public Long getCompanyId()
        {
            return companyId;
        }

        public void setCompanyId(Long companyId)
        {
            this.companyId = companyId;
        }

        public String getFileUrl()
        {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl)
        {
            this.fileUrl = fileUrl;
        }

        public String getFileType()
        {
            return fileType;
        }

        public void setFileType(String fileType)
        {
            this.fileType = fileType;
        }

        public String getRawText()
        {
            return rawText;
        }

        public void setRawText(String rawText)
        {
            this.rawText = rawText;
        }
    }

    public static class ConfirmRequest
    {
        private String subject;
        private Integer entryType;
        private String entryRemark;

        public String getSubject()
        {
            return subject;
        }

        public void setSubject(String subject)
        {
            this.subject = subject;
        }

        public Integer getEntryType()
        {
            return entryType;
        }

        public void setEntryType(Integer entryType)
        {
            this.entryType = entryType;
        }

        public String getEntryRemark()
        {
            return entryRemark;
        }

        public void setEntryRemark(String entryRemark)
        {
            this.entryRemark = entryRemark;
        }
    }
}
