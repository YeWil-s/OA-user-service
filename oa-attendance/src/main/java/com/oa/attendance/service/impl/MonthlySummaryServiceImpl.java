package com.oa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.attendance.client.UserServiceClient;
import com.oa.attendance.entity.AttRecord;
import com.oa.attendance.entity.AttShift;
import com.oa.attendance.mapper.AttRecordMapper;
import com.oa.attendance.mapper.AttShiftMapper;
import com.oa.attendance.mapper.UserShiftMapper;
import com.oa.attendance.service.IMonthlySummaryService;
import com.oa.attendance.vo.MonthlyAttendanceVO;
import com.oa.common.remote.DeptInfo;
import com.oa.common.remote.UserInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonthlySummaryServiceImpl implements IMonthlySummaryService {

    private final AttRecordMapper attRecordMapper;
    private final UserServiceClient userServiceClient;

    public MonthlySummaryServiceImpl(AttRecordMapper attRecordMapper,
                                      UserServiceClient userServiceClient) {
        this.attRecordMapper = attRecordMapper;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public List<MonthlyAttendanceVO> monthlyAttendanceSummary(String month) {
        YearMonth ym = YearMonth.parse(month);
        List<AttRecord> records = attRecordMapper.selectList(new LambdaQueryWrapper<AttRecord>()
                .ge(AttRecord::getRecordDate, ym.atDay(1))
                .le(AttRecord::getRecordDate, ym.atEndOfMonth()));
        if (records.isEmpty()) {
            return List.of();
        }

        Map<Long, UserInfo> userMap = userServiceClient.mapUsers(
                records.stream().map(AttRecord::getUserId).distinct().toList());
        Map<Long, DeptInfo> deptMap = userServiceClient.mapDepts();

        Map<Long, List<AttRecord>> byDept = records.stream()
                .filter(r -> userMap.containsKey(r.getUserId()))
                .collect(Collectors.groupingBy(r -> userMap.get(r.getUserId()).getDeptId()));

        List<MonthlyAttendanceVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<AttRecord>> entry : byDept.entrySet()) {
            Long deptId = entry.getKey();
            List<AttRecord> deptRecords = entry.getValue();
            DeptInfo dept = deptMap.get(deptId);

            MonthlyAttendanceVO vo = new MonthlyAttendanceVO();
            vo.setDeptId(deptId);
            vo.setDeptName(dept != null ? dept.getDeptName() : "未知部门");
            vo.setTotalEmployees((long) deptRecords.stream().map(AttRecord::getUserId).distinct().count());
            vo.setNormalCount(countByType(deptRecords, null)); // 正常=非异常打卡
            vo.setLateCount(countByType(deptRecords, null));  // 需要有状态字段
            vo.setEarlyCount(0L);
            vo.setAbsentCount(0L);
            vo.setLeaveCount(countByType(deptRecords, 3));
            vo.setOvertimeTotal(BigDecimal.ZERO);
            result.add(vo);
        }
        return result;
    }

    private long countByType(List<AttRecord> records, Integer punchType) {
        return records.stream()
                .filter(r -> punchType == null ? r.getPunchType() == null : punchType.equals(r.getPunchType()))
                .count();
    }
}
