package com.oa.asset.service.impl;

import com.oa.asset.client.UserDirectoryClient;
import com.oa.asset.service.UserDirectoryService;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

/** 通过 oa-user-service 验证员工、部门、岗位及岗位归属关系。 */
@Service
public class UserDirectoryServiceImpl implements UserDirectoryService {
    private static final int MASTER_DATA_INVALID = 60008;
    private static final int USER_SERVICE_UNAVAILABLE = 60009;
    private final UserDirectoryClient client;

    public UserDirectoryServiceImpl(UserDirectoryClient client) { this.client = client; }

    @Override
    public UserDirectoryClient.EmployeeRef requireEmployee(Long userId) {
        try {
            Result<UserDirectoryClient.EmployeeRef> result = client.employee(userId);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BusinessException(MASTER_DATA_INVALID, "员工不存在或不可用");
            }
            return result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(USER_SERVICE_UNAVAILABLE, "用户服务暂时不可用，无法校验员工信息");
        }
    }

    @Override
    public void requireDepartmentAndPosition(Long deptId, Long positionId) {
        if (deptId == null || positionId == null) throw new BusinessException(400, "部门和岗位不能为空");
        try {
            Result<List<UserDirectoryClient.DeptRef>> deptResult = client.departments();
            if (deptResult == null || deptResult.getCode() != 200 || !containsDept(deptResult.getData(), deptId)) {
                throw new BusinessException(MASTER_DATA_INVALID, "部门不存在");
            }
            Result<UserDirectoryClient.PageRef<UserDirectoryClient.PositionRef>> positionResult =
                    client.positions(1, 10000, deptId);
            List<UserDirectoryClient.PositionRef> positions = positionResult == null || positionResult.getData() == null
                    ? List.of() : positionResult.getData().getRecords();
            boolean matched = positionResult != null && positionResult.getCode() == 200 && positions != null
                    && positions.stream().anyMatch(p -> positionId.equals(p.getId()) && deptId.equals(p.getDeptId()));
            if (!matched) throw new BusinessException(MASTER_DATA_INVALID, "岗位不存在或不属于所选部门");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(USER_SERVICE_UNAVAILABLE, "用户服务暂时不可用，无法校验部门和岗位");
        }
    }

    private boolean containsDept(List<UserDirectoryClient.DeptRef> nodes, Long id) {
        if (nodes == null) return false;
        for (UserDirectoryClient.DeptRef node : nodes) {
            if (id.equals(node.getId()) || containsDept(node.getChildren(), id)) return true;
        }
        return false;
    }
}
