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
import com.aifc.system.domain.biz.BizInventory;
import com.aifc.system.service.biz.IBizInventoryService;

@RestController
@RequestMapping("/aifc/inventory")
public class BizInventoryController extends BaseController
{
    @Autowired
    private IBizInventoryService bizInventoryService;

    @PreAuthorize("@ss.hasPermi('aifc:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizInventory bizInventory)
    {
        startPage();
        List<BizInventory> list = bizInventoryService.selectBizInventoryList(bizInventory);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('aifc:inventory:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(bizInventoryService.selectBizInventoryById(id));
    }

    @PreAuthorize("@ss.hasPermi('aifc:inventory:add')")
    @Log(title = "AIFC Inventory", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizInventory bizInventory)
    {
        return toAjax(bizInventoryService.insertBizInventory(bizInventory));
    }

    @PreAuthorize("@ss.hasPermi('aifc:inventory:edit')")
    @Log(title = "AIFC Inventory", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizInventory bizInventory)
    {
        return toAjax(bizInventoryService.updateBizInventory(bizInventory));
    }

    @PreAuthorize("@ss.hasPermi('aifc:inventory:remove')")
    @Log(title = "AIFC Inventory", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizInventoryService.deleteBizInventoryByIds(ids));
    }
}
