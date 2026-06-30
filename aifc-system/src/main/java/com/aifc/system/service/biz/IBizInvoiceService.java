package com.aifc.system.service.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizInvoice;

public interface IBizInvoiceService
{
    BizInvoice selectBizInvoiceById(Long id);

    List<BizInvoice> selectBizInvoiceList(BizInvoice bizInvoice);

    int insertBizInvoice(BizInvoice bizInvoice);

    int updateBizInvoice(BizInvoice bizInvoice);

    int deleteBizInvoiceById(Long id);

    int deleteBizInvoiceByIds(Long[] ids);

    BizInvoice recognizeAndCreateDraft(Long companyId, String fileUrl, String fileType, String rawText);

    int confirmAndCreateVoucher(Long id, String subject, Integer entryType, String entryRemark);
}
