package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.user.dto.VisualDeptStatsDTO;
import com.oa.user.entity.SysDept;
import com.oa.user.entity.SysUser;
import com.oa.user.mapper.SysDeptMapper;
import com.oa.user.mapper.SysUserMapper;
import com.oa.user.service.IVisualStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisualStatisticsServiceImpl implements IVisualStatisticsService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;

    public VisualStatisticsServiceImpl(SysDeptMapper sysDeptMapper, SysUserMapper sysUserMapper) {
        this.sysDeptMapper = sysDeptMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<VisualDeptStatsDTO> departmentStats(String month) {
        YearMonth statMonth = normalizeMonth(month);
        var startDate = statMonth.atDay(1);
        var endDate = statMonth.plusMonths(1).atDay(1);
        var startTime = startDate.atStartOfDay();
        var endTime = endDate.atStartOfDay();

        Map<Long, DeptAccumulator> stats = new LinkedHashMap<>();
        sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getStatus, 1)
                        .eq(SysDept::getIsDeleted, 0)
                        .orderByAsc(SysDept::getSortOrder)
                        .orderByAsc(SysDept::getId))
                .forEach(dept -> stats.put(dept.getId(), new DeptAccumulator(dept.getDeptName())));

        sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getIsDeleted, 0)
                        .isNotNull(SysUser::getDeptId))
                .forEach(user -> {
                    DeptAccumulator item = stats.get(user.getDeptId());
                    if (item == null) {
                        return;
                    }
                    if (Integer.valueOf(1).equals(user.getStatus())
                            && (user.getEntryDate() == null || user.getEntryDate().isBefore(endDate))) {
                        item.activeEmployees++;
                    }
                    if (user.getEntryDate() != null
                            && !user.getEntryDate().isBefore(startDate)
                            && user.getEntryDate().isBefore(endDate)) {
                        item.newHires++;
                    }
                    if (Integer.valueOf(0).equals(user.getStatus())
                            && user.getUpdateTime() != null
                            && !user.getUpdateTime().isBefore(startTime)
                            && user.getUpdateTime().isBefore(endTime)) {
                        item.resignations++;
                    }
                });

        return stats.entrySet().stream()
                .map(entry -> new VisualDeptStatsDTO(entry.getKey(), entry.getValue().deptName,
                        entry.getValue().activeEmployees, entry.getValue().newHires, entry.getValue().resignations))
                .toList();
    }

    private YearMonth normalizeMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim(), MONTH_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "统计月份格式应为 yyyy-MM");
        }
    }

    private static class DeptAccumulator {
        private final String deptName;
        private int activeEmployees;
        private int newHires;
        private int resignations;

        private DeptAccumulator(String deptName) {
            this.deptName = deptName;
        }
    }
}
