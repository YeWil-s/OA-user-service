package com.oa.asset.service;

import com.oa.asset.client.UserDirectoryClient;

/** 隔离用户主数据远程调用，供资产和人事业务统一校验。 */
public interface UserDirectoryService {
    UserDirectoryClient.EmployeeRef requireEmployee(Long userId);
    void requireDepartmentAndPosition(Long deptId, Long positionId);
}
