package com.oa.attendance.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.attendance.client.NoticeServiceClient;
import com.oa.attendance.client.UserServiceClient;
import com.oa.attendance.entity.AttRecord;
import com.oa.attendance.entity.AttSchedule;
import com.oa.attendance.entity.AttShift;
import com.oa.attendance.entity.UserShift;
import com.oa.attendance.mapper.AttRecordMapper;
import com.oa.attendance.mapper.AttScheduleMapper;
import com.oa.attendance.mapper.AttShiftMapper;
import com.oa.attendance.mapper.UserShiftMapper;
import com.oa.common.remote.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MissingPunchReminderJob {

    private static final Logger log = LoggerFactory.getLogger(MissingPunchReminderJob.class);

    private static final int MSG_TYPE_ATTENDANCE = 2;

    private final AttRecordMapper attRecordMapper;
    private final AttScheduleMapper attScheduleMapper;
    private final AttShiftMapper attShiftMapper;
    private final UserShiftMapper userShiftMapper;
    private final UserServiceClient userServiceClient;
    private final NoticeServiceClient noticeServiceClient;

    public MissingPunchReminderJob(AttRecordMapper attRecordMapper,
                                   AttScheduleMapper attScheduleMapper,
                                   AttShiftMapper attShiftMapper,
                                   UserShiftMapper userShiftMapper,
                                   UserServiceClient userServiceClient,
                                   NoticeServiceClient noticeServiceClient) {
        this.attRecordMapper = attRecordMapper;
        this.attScheduleMapper = attScheduleMapper;
        this.attShiftMapper = attShiftMapper;
        this.userShiftMapper = userShiftMapper;
        this.userServiceClient = userServiceClient;
        this.noticeServiceClient = noticeServiceClient;
    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void remindMissingPunch() {
        log.info("开始执行缺卡提醒任务");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        try {
            List<UserInfo> allUsers = userServiceClient.listAllActiveUsers();
            if (allUsers.isEmpty()) {
                log.info("无在职用户");
                return;
            }

            Set<Long> punchedUserIds = attRecordMapper.selectList(
                            new LambdaQueryWrapper<AttRecord>()
                                    .eq(AttRecord::getRecordDate, today)
                                    .isNotNull(AttRecord::getPunchInTime))
                    .stream()
                    .map(AttRecord::getUserId)
                    .collect(Collectors.toSet());

            int reminded = 0;
            for (UserInfo user : allUsers) {
                if (punchedUserIds.contains(user.getId())) {
                    continue; // 已打卡
                }

                // 查当天排班
                AttSchedule schedule = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                        .eq(AttSchedule::getUserId, user.getId())
                        .eq(AttSchedule::getScheduleDate, today)
                        .last("limit 1"));
                // 请假状态跳过
                if (schedule != null && Integer.valueOf(2).equals(schedule.getStatus())) {
                    continue;
                }

                AttShift shift = null;
                if (schedule != null) {
                    shift = attShiftMapper.selectById(schedule.getShiftId());
                } else {
                    // fallback 固定班次
                    UserShift userShift = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                            .eq(UserShift::getUserId, user.getId())
                            .last("limit 1"));
                    if (userShift != null) {
                        shift = attShiftMapper.selectById(userShift.getShiftId());
                    }
                }

                // 无班次则跳过
                if (shift == null || shift.getStartTime() == null) {
                    continue;
                }

                // 当前时间超过班次开始时间30分钟后才提醒
                LocalTime remindAfter = shift.getStartTime().plusMinutes(30);
                if (now.isBefore(remindAfter)) {
                    continue;
                }

                String shiftInfo = shift.getShiftName() + " " + shift.getStartTime() + "-" + shift.getEndTime();
                noticeServiceClient.sendMessage(user.getId(),
                        "打卡提醒",
                        "您今天尚未完成上班打卡（班次：" + shiftInfo + "），请及时打卡。",
                        MSG_TYPE_ATTENDANCE,
                        null);
                reminded++;
            }
            log.info("缺卡提醒完成: 总用户={}, 已打卡={}, 已提醒={}", allUsers.size(), punchedUserIds.size(), reminded);
        } catch (Exception e) {
            log.error("缺卡提醒任务失败", e);
        }
    }
}
