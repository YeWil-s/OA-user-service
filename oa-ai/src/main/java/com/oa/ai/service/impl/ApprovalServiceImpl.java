package com.oa.ai.service.impl;

import com.oa.ai.dto.ApprovalSubmitDTO;
import com.oa.ai.entity.AppApplication;
import com.oa.ai.mapper.ApplicationMapper;
import com.oa.ai.service.IApprovalService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * TODO: 当前为临时方案，直接写 approval_db.app_application 表。
 * 待 oa-approval 服务开发完成后，改为通过 Nacos 服务发现 + Feign/RestTemplate 调用：
 * <pre>
 *   restTemplate.postForObject("http://oa-approval-service/api/approval/submit", dto, String.class);
 * </pre>
 * 届时删除本类中的 ApplicationMapper 注入和所有直接 DB 操作。
 */
@Service
public class ApprovalServiceImpl implements IApprovalService {

    // TODO: 待 oa-approval 上线后替换为 Feign 调用
    private final ApplicationMapper applicationMapper;

    public ApprovalServiceImpl(ApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    @Override
    public String submitApplication(ApprovalSubmitDTO dto, Long userId, Long deptId) {
        String applicationNo = generateApplicationNo();

        AppApplication app = new AppApplication();
        app.setApplicationNo(applicationNo);
        app.setUserId(userId);
        app.setDeptId(deptId != null ? deptId : 1L);
        app.setAppType(dto.getAppType());
        app.setLeaveType(dto.getLeaveType());
        app.setStartTime(dto.getStartTime());
        app.setEndTime(dto.getEndTime());

        if (dto.getDuration() != null) {
            app.setDuration(dto.getDuration());
        } else {
            long hours = ChronoUnit.HOURS.between(dto.getStartTime(), dto.getEndTime());
            BigDecimal duration = BigDecimal.valueOf(hours).divide(BigDecimal.valueOf(8), 1, RoundingMode.HALF_UP);
            app.setDuration(duration);
        }

        app.setReason(dto.getReason());
        app.setStatus(1); // pending approval
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());

        applicationMapper.insert(app);
        return applicationNo;
    }

    @Override
    public AppApplication getApplication(Long id) {
        return applicationMapper.selectById(id);
    }

    private String generateApplicationNo() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxNo = applicationMapper.selectMaxApplicationNoToday();
        int seq = 1;
        if (maxNo != null && maxNo.length() >= 16) {
            try {
                seq = Integer.parseInt(maxNo.substring(maxNo.length() - 3)) + 1;
            } catch (NumberFormatException e) {
                // use default seq=1
            }
        }
        return "LV" + today + String.format("%03d", seq);
    }
}
