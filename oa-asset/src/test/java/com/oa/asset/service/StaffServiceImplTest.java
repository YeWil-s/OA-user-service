package com.oa.asset.service;

import com.oa.asset.client.UserDirectoryClient;
import com.oa.asset.dto.EmployeeArchiveDTO;
import com.oa.asset.dto.StaffChangeDTO;
import com.oa.asset.entity.EmployeeArchive;
import com.oa.asset.entity.StaffChange;
import com.oa.asset.mapper.EmployeeArchiveMapper;
import com.oa.asset.mapper.StaffChangeMapper;
import com.oa.asset.service.impl.StaffServiceImpl;
import com.oa.asset.service.UserDirectoryService;
import com.oa.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 员工档案、合同和人事变动业务单元测试。 */
@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {
    @Mock private EmployeeArchiveMapper archiveMapper;
    @Mock private StaffChangeMapper changeMapper;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private UserDirectoryClient userDirectoryClient;
    @Mock private com.oa.asset.client.NoticeServiceClient noticeServiceClient;
    private StaffServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StaffServiceImpl(archiveMapper, changeMapper,
                userDirectoryService, userDirectoryClient, noticeServiceClient);
    }

    @Test
    @DisplayName("员工没有档案时新增档案")
    void saveArchiveInsertsWhenMissing() {
        when(archiveMapper.selectOne(any())).thenReturn(null);
        service.saveArchive(7L, archiveDto());
        ArgumentCaptor<EmployeeArchive> captor = ArgumentCaptor.forClass(EmployeeArchive.class);
        verify(archiveMapper).insert(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertNotNull(captor.getValue().getCreateTime());
        verify(archiveMapper, never()).updateById(any(EmployeeArchive.class));
    }

    @Test
    @DisplayName("员工已有档案时更新原档案")
    void saveArchiveUpdatesWhenPresent() {
        EmployeeArchive existing = new EmployeeArchive(); existing.setId(1L); existing.setUserId(7L);
        when(archiveMapper.selectOne(any())).thenReturn(existing);
        service.saveArchive(7L, archiveDto());
        verify(archiveMapper).updateById(existing);
        verify(archiveMapper, never()).insert(any(EmployeeArchive.class));
        assertEquals("计算机科学", existing.getMajor());
    }

    @Test
    @DisplayName("合同结束日期早于开始日期时拒绝保存")
    void saveArchiveRejectsInvalidContractDates() {
        EmployeeArchiveDTO dto = archiveDto();
        dto.setContractStart(LocalDate.of(2027, 1, 1));
        dto.setContractEnd(LocalDate.of(2026, 12, 31));
        BusinessException exception = assertThrows(BusinessException.class, () -> service.saveArchive(7L, dto));
        assertEquals(400, exception.getCode());
        verifyNoInteractions(archiveMapper);
    }

    @Test
    @DisplayName("学历枚举必须在1到5之间")
    void saveArchiveRejectsInvalidEducation() {
        EmployeeArchiveDTO dto = archiveDto(); dto.setEducation(6);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.saveArchive(7L, dto));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("查询不存在的员工档案返回业务异常")
    void getArchiveRejectsMissingArchive() {
        when(archiveMapper.selectOne(any())).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.getArchive(7L));
        assertEquals(60006, exception.getCode());
    }

    @Test
    @DisplayName("合同预警天数必须在允许范围内")
    void expiringContractsRejectsInvalidDays() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.expiringContracts(1, 10, -1));
        assertEquals(400, exception.getCode());
        verifyNoInteractions(archiveMapper);
    }

    @Test
    @DisplayName("新增人事变动时复制业务字段并设置创建时间")
    void createChangePersistsRecord() {
        UserDirectoryClient.EmployeeRef emp = new UserDirectoryClient.EmployeeRef();
        emp.setDeptId(1L); emp.setPositionId(1L);
        when(userDirectoryService.requireEmployee(7L)).thenReturn(emp);

        StaffChangeDTO dto = changeDto();
        service.createChange(dto);
        ArgumentCaptor<StaffChange> captor = ArgumentCaptor.forClass(StaffChange.class);
        verify(changeMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getChangeType());
        assertEquals(7L, captor.getValue().getUserId());
        assertNotNull(captor.getValue().getCreateTime());
    }

    @Test
    @DisplayName("更新不存在的人事变动记录返回业务异常")
    void updateChangeRejectsMissingRecord() {
        when(changeMapper.selectById(99L)).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.updateChange(99L, changeDto()));
        assertEquals(60007, exception.getCode());
    }

    @Test
    @DisplayName("删除人事变动前检查记录是否存在")
    void deleteChangeDeletesExistingRecord() {
        StaffChange record = new StaffChange(); record.setId(1L);
        when(changeMapper.selectById(1L)).thenReturn(record);
        service.deleteChange(1L);
        verify(changeMapper).deleteById(1L);
    }

    private EmployeeArchiveDTO archiveDto() {
        EmployeeArchiveDTO dto = new EmployeeArchiveDTO();
        dto.setEducation(3); dto.setMajor("计算机科学"); dto.setEmergencyContact("张三");
        dto.setEmergencyPhone("13800138000"); dto.setContractStart(LocalDate.of(2026, 7, 1));
        dto.setContractEnd(LocalDate.of(2027, 6, 30));
        return dto;
    }
    private StaffChangeDTO changeDto() {
        StaffChangeDTO dto = new StaffChangeDTO(); dto.setUserId(7L); dto.setChangeType(3);
        dto.setBeforeDept(1L); dto.setAfterDept(2L); dto.setChangeDate(LocalDate.of(2026, 7, 21));
        dto.setRemark("调岗测试"); return dto;
    }
}
