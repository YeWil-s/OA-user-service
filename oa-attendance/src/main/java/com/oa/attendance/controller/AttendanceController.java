package com.oa.attendance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.service.IAttendanceService;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ShiftVO;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    public AttendanceController(IAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
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
}
