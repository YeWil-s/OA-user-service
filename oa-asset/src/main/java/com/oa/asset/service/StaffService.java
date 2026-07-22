package com.oa.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.asset.dto.EmployeeArchiveDTO;
import com.oa.asset.dto.StaffChangeDTO;
import com.oa.asset.entity.EmployeeArchive;
import com.oa.asset.entity.StaffChange;

public interface StaffService {
    EmployeeArchive getArchive(Long userId);
    void saveArchive(Long userId, EmployeeArchiveDTO dto);
    IPage<EmployeeArchive> contracts(int pageNum, int pageSize, Long userId);
    IPage<EmployeeArchive> expiringContracts(int pageNum, int pageSize, int days);
    IPage<StaffChange> changes(int pageNum, int pageSize, Long userId, Integer changeType);
    void createChange(StaffChangeDTO dto);
    void updateChange(Long id, StaffChangeDTO dto);
    void deleteChange(Long id);
}
