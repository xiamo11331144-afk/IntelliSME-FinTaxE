package com.aifc.system.mapper.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizInvoice;

public interface BizInvoiceMapper
{
    BizInvoice selectBizInvoiceById(Long id);

    List<BizInvoice> selectBizInvoiceList(BizInvoice bizInvoice);

    int insertBizInvoice(BizInvoice bizInvoice);

    int updateBizInvoice(BizInvoice bizInvoice);

    int deleteBizInvoiceById(Long id);

    int deleteBizInvoiceByIds(Long[] ids);

    int insertBizVoucherByInvoice(BizInvoice bizInvoice);

    int countVoucherByInvoiceId(Long id);

    int countInvoiceDuplicate(BizInvoice bizInvoice);

    int countSeriesNeighbor(BizInvoice bizInvoice);
}
