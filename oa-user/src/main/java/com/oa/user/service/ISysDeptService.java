package com.oa.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.user.dto.DeptDTO;
import com.oa.user.entity.SysDept;
import com.oa.user.vo.DeptTreeVO;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {

    List<DeptTreeVO> getDeptTree();

    void addDept(DeptDTO dto);

    void updateDept(Long id, DeptDTO dto);

    void deleteDept(Long id);
}
