package com.oa.attendance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.FieldWorkDTO;
import com.oa.attendance.dto.LeaveDTO;
import com.oa.attendance.dto.OvertimeDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ScheduleDTO;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.dto.VisualAttendanceStatsDTO;
import com.oa.attendance.dto.VisualTodayAttendanceDTO;
import com.oa.attendance.service.IAttendanceService;
import com.oa.attendance.service.IMonthlySummaryService;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ScheduleVO;
import com.oa.attendance.vo.ShiftVO;
import com.oa.attendance.vo.UserShiftVO;
import com.oa.attendance.vo.MonthlyAttendanceVO;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "考勤服务")
@Validated
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final IAttendanceService attendanceService;
    private final IMonthlySummaryService monthlySummaryService;

    public AttendanceController(IAttendanceService attendanceService,
                                 IMonthlySummaryService monthlySummaryService) {
        this.attendanceService = attendanceService;
        this.monthlySummaryService = monthlySummaryService;
    }

    @Operation(summary = "班次分页列表")
    @GetMapping("/shifts")
    public Result<IPage<ShiftVO>> pageShifts(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(attendanceService.pageShifts(pageNum, pageSize));
    }

    @Operation(summary = "班次详情")
    @GetMapping("/shifts/{id}")
    public Result<ShiftVO> getShift(@PathVariable Long id) {
        return Result.success(attendanceService.getShift(id));
    }

    @Operation(summary = "新增班次")
    @PostMapping("/shifts")
    public Result<ShiftVO> createShift(@Valid @RequestBody ShiftDTO dto) {
        return Result.success("班次创建成功", attendanceService.createShift(dto));
    }

    @Operation(summary = "编辑班次")
    @PutMapping("/shifts/{id}")
    public Result<ShiftVO> updateShift(@PathVariable Long id, @Valid @RequestBody ShiftDTO dto) {
        return Result.success("班次更新成功", attendanceService.updateShift(id, dto));
    }

    @Operation(summary = "删除班次")
    @DeleteMapping("/shifts/{id}")
    public Result<Void> deleteShift(@PathVariable Long id) {
        attendanceService.deleteShift(id);
        return Result.success();
    }

    @Operation(summary = "用户班次分配")
    @PostMapping("/user-shifts")
    public Result<Void> assignShift(@Valid @RequestBody UserShiftDTO dto) {
        attendanceService.assignShift(dto);
        return Result.success();
    }

    @Operation(summary = "上班打卡")
    @PostMapping("/punch/in")
    public Result<PunchVO> punchIn(@RequestBody(required = false) PunchDTO dto) {
        return Result.success(attendanceService.punchIn(dto));
    }

    @Operation(summary = "下班打卡")
    @PostMapping("/punch/out")
    public Result<PunchVO> punchOut(@RequestBody(required = false) PunchDTO dto) {
        return Result.success(attendanceService.punchOut(dto));
    }

    @Operation(summary = "个人考勤记录")
    @GetMapping("/records/mine")
    public Result<IPage<AttendanceRecordVO>> myRecords(@Valid AttendanceRecordQueryDTO dto) {
        return Result.success(attendanceService.myRecords(dto));
    }

    @Operation(summary = "部门考勤记录")
    @GetMapping("/records/dept")
    public Result<IPage<AttendanceRecordVO>> deptRecords(@Valid AttendanceRecordQueryDTO dto) {
        return Result.success(attendanceService.deptRecords(dto));
    }

    @Operation(summary = "全公司考勤记录")
    @GetMapping("/records/all")
    public Result<IPage<AttendanceRecordVO>> allRecords(@Valid AttendanceRecordQueryDTO dto) {
        return Result.success(attendanceService.allRecords(dto));
    }

    @Operation(summary = "批量标记请假")
    @PostMapping("/leave")
    public Result<Void> markLeave(@Valid @RequestBody LeaveDTO dto) {
        attendanceService.markLeave(dto);
        return Result.success();
    }

    @Operation(summary = "取消请假标记")
    @DeleteMapping("/leave/{applicationId}")
    public Result<Void> cancelLeave(@PathVariable Long applicationId) {
        attendanceService.cancelLeave(applicationId);
        return Result.success();
    }

    @Operation(summary = "批量标记加班")
    @PostMapping("/overtime")
    public Result<Void> markOvertime(@Valid @RequestBody OvertimeDTO dto) {
        attendanceService.markOvertime(dto);
        return Result.success();
    }

    @Operation(summary = "取消加班标记")
    @DeleteMapping("/overtime/{applicationId}")
    public Result<Void> cancelOvertime(@PathVariable Long applicationId) {
        attendanceService.cancelOvertime(applicationId);
        return Result.success();
    }

    @Operation(summary = "批量标记外勤")
    @PostMapping("/fieldwork")
    public Result<Void> markFieldWork(@Valid @RequestBody FieldWorkDTO dto) {
        attendanceService.markFieldWork(dto);
        return Result.success();
    }

    @Operation(summary = "取消外勤标记")
    @DeleteMapping("/fieldwork/{applicationId}")
    public Result<Void> cancelFieldWork(@PathVariable Long applicationId) {
        attendanceService.cancelFieldWork(applicationId);
        return Result.success();
    }

    @Operation(summary = "部门月考勤汇总")
    @GetMapping("/records/monthly-summary")
    public Result<List<MonthlyAttendanceVO>> monthlySummary(@RequestParam String month) {
        return Result.success(monthlySummaryService.monthlyAttendanceSummary(month));
    }

    @Operation(summary = "我的排班")
    @GetMapping("/schedules/mine")
    public Result<List<ScheduleVO>> mySchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.mySchedules(startDate, endDate));
    }

    @Operation(summary = "部门排班查看")
    @GetMapping("/schedules/dept")
    public Result<List<ScheduleVO>> deptSchedules(
            @RequestParam Long deptId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.deptSchedules(deptId, startDate, endDate));
    }

    @Operation(summary = "指定用户排班（管理员）")
    @GetMapping("/schedules/user/{userId}")
    public Result<List<ScheduleVO>> userSchedules(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.userSchedules(userId, startDate, endDate));
    }

    @Operation(summary = "批量排班")
    @PostMapping("/schedules")
    public Result<Void> batchSchedule(@Valid @RequestBody ScheduleDTO dto) {
        attendanceService.batchSchedule(dto.getItems());
        return Result.success();
    }

    @Operation(summary = "所有人员默认班次")
    @GetMapping("/user-shifts/all")
    public Result<List<UserShiftVO>> allUserShifts() {
        return Result.success(attendanceService.allUserShifts());
    }

    @Operation(summary = "手动生成每日考勤汇总")
    @PostMapping("/daily-summary/generate")
    public Result<Void> generateDailySummary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        attendanceService.generateDailySummary(date);
        return Result.success();
    }

    @GetMapping("/internal/visual/monthly")
    public Result<List<VisualAttendanceStatsDTO>> monthlyVisualStats(@RequestParam(required = false) String month) {
        return Result.success(attendanceService.monthlyVisualStats(month));
    }

    @GetMapping("/internal/visual/today")
    public Result<VisualTodayAttendanceDTO> todayVisualStats() {
        return Result.success(attendanceService.todayVisualStats());
    }
}
