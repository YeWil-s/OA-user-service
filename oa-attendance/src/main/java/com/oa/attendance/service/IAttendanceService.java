package com.oa.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.LeaveDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ScheduleItem;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.dto.VisualAttendanceStatsDTO;
import com.oa.attendance.dto.VisualTodayAttendanceDTO;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ScheduleVO;
import com.oa.attendance.vo.ShiftVO;
import com.oa.attendance.vo.UserShiftVO;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceService {

    IPage<ShiftVO> pageShifts(Integer pageNum, Integer pageSize);

    ShiftVO getShift(Long id);

    ShiftVO createShift(ShiftDTO dto);

    ShiftVO updateShift(Long id, ShiftDTO dto);

    void deleteShift(Long id);

    void assignShift(UserShiftDTO dto);

    PunchVO punchIn(PunchDTO dto);

    PunchVO punchOut(PunchDTO dto);

    IPage<AttendanceRecordVO> myRecords(AttendanceRecordQueryDTO dto);

    IPage<AttendanceRecordVO> deptRecords(AttendanceRecordQueryDTO dto);

    IPage<AttendanceRecordVO> allRecords(AttendanceRecordQueryDTO dto);

    void markLeave(LeaveDTO dto);

    void cancelLeave(Long applicationId);

    List<ScheduleVO> mySchedules(LocalDate startDate, LocalDate endDate);

    List<ScheduleVO> deptSchedules(Long deptId, LocalDate startDate, LocalDate endDate);

    List<ScheduleVO> userSchedules(Long userId, LocalDate startDate, LocalDate endDate);

    void batchSchedule(List<ScheduleItem> items);

    List<UserShiftVO> allUserShifts();

    List<VisualAttendanceStatsDTO> monthlyVisualStats(String month);

    VisualTodayAttendanceDTO todayVisualStats();

    void generateDailySummary(LocalDate date);
}
