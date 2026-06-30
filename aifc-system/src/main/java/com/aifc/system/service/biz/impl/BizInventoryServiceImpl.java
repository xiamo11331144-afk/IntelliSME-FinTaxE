package com.aifc.system.service.biz.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aifc.common.exception.ServiceException;
import com.aifc.common.utils.DateUtils;
import com.aifc.common.utils.StringUtils;
import com.aifc.system.domain.biz.BizInventory;
import com.aifc.system.mapper.biz.BizInventoryMapper;
import com.aifc.system.service.biz.IBizInventoryService;

@Service
public class BizInventoryServiceImpl implements IBizInventoryService
{
    @Autowired
    private BizInventoryMapper bizInventoryMapper;

    @Autowired
    private AifcDataScopeService aifcDataScopeService;

    @Override
    public BizInventory selectBizInventoryById(Long id)
    {
        BizInventory inventory = bizInventoryMapper.selectBizInventoryById(id);
        if (inventory != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(inventory.getCompanyId());
        }
        return inventory;
    }

    @Override
    public List<BizInventory> selectBizInventoryList(BizInventory bizInventory)
    {
        applyCompanyScope(bizInventory);
        return bizInventoryMapper.selectBizInventoryList(bizInventory);
    }

    @Override
    public int insertBizInventory(BizInventory bizInventory)
    {
        validateBasic(bizInventory);
        aifcDataScopeService.ensureCompanyAccessible(bizInventory.getCompanyId());
        fillDiff(bizInventory);
        bizInventory.setCreateTime(DateUtils.getNowDate());
        return bizInventoryMapper.insertBizInventory(bizInventory);
    }

    @Override
    public int updateBizInventory(BizInventory bizInventory)
    {
        BizInventory exists = bizInventoryMapper.selectBizInventoryById(bizInventory.getId());
        if (exists == null)
        {
            throw new ServiceException("inventory not found");
        }
        aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        if (bizInventory.getCompanyId() != null && !bizInventory.getCompanyId().equals(exists.getCompanyId()))
        {
            throw new ServiceException("companyId cannot be changed");
        }
        fillDiff(bizInventory);
        bizInventory.setCompanyId(null);
        bizInventory.setUpdateTime(DateUtils.getNowDate());
        return bizInventoryMapper.updateBizInventory(bizInventory);
    }

    @Override
    public int deleteBizInventoryById(Long id)
    {
        BizInventory exists = bizInventoryMapper.selectBizInventoryById(id);
        if (exists != null)
        {
            aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
        }
        return bizInventoryMapper.deleteBizInventoryById(id);
    }

    @Override
    public int deleteBizInventoryByIds(Long[] ids)
    {
        if (ids != null)
        {
            for (Long id : ids)
            {
                BizInventory exists = bizInventoryMapper.selectBizInventoryById(id);
                if (exists != null)
                {
                    aifcDataScopeService.ensureCompanyAccessible(exists.getCompanyId());
                }
            }
        }
        return bizInventoryMapper.deleteBizInventoryByIds(ids);
    }

    private void validateBasic(BizInventory bizInventory)
    {
        if (bizInventory.getCompanyId() == null)
        {
            throw new ServiceException("companyId is required");
        }
        if (StringUtils.isEmpty(bizInventory.getReportMonth()))
        {
            throw new ServiceException("reportMonth is required");
        }
    }

    private void fillDiff(BizInventory bizInventory)
    {
        BigDecimal book = bizInventory.getBookAmount();
        BigDecimal real = bizInventory.getRealAmount();
        if (book == null || real == null)
        {
            return;
        }
        BigDecimal diff = real.subtract(book);
        bizInventory.setDiffAmount(diff);
        if (book.compareTo(BigDecimal.ZERO) == 0)
        {
            bizInventory.setDiffRate(BigDecimal.ZERO);
            return;
        }
        BigDecimal rate = diff.abs()
            .multiply(new BigDecimal("100"))
            .divide(book.abs(), 2, RoundingMode.HALF_UP);
        bizInventory.setDiffRate(rate);
    }

    private void applyCompanyScope(BizInventory query)
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
