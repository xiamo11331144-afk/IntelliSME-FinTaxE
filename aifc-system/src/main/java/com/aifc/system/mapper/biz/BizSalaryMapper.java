package com.aifc.system.mapper.biz;

import java.util.List;
import com.aifc.system.domain.biz.BizSalary;

public interface BizSalaryMapper
{
    BizSalary selectBizSalaryById(Long id);

    List<BizSalary> selectBizSalaryList(BizSalary bizSalary);

    int insertBizSalary(BizSalary bizSalary);

    int updateBizSalary(BizSalary bizSalary);

    int deleteBizSalaryById(Long id);

    int deleteBizSalaryByIds(Long[] ids);
}
