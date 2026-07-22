package com.oa.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ShiftVO;

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
}
