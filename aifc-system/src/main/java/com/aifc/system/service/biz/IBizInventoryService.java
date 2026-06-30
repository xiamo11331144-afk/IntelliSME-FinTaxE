package com.aifc.system.service.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizInventory;

public interface IBizInventoryService
{
    BizInventory selectBizInventoryById(Long id);

    List<BizInventory> selectBizInventoryList(BizInventory bizInventory);

    int insertBizInventory(BizInventory bizInventory);

    int updateBizInventory(BizInventory bizInventory);

    int deleteBizInventoryById(Long id);

    int deleteBizInventoryByIds(Long[] ids);
}
