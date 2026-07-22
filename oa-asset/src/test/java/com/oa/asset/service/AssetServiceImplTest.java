package com.oa.asset.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.oa.asset.dto.AssetDTO;
import com.oa.asset.dto.BorrowDTO;
import com.oa.asset.entity.Asset;
import com.oa.asset.entity.AssetRecord;
import com.oa.asset.mapper.AssetMapper;
import com.oa.asset.mapper.AssetRecordMapper;
import com.oa.asset.service.impl.AssetServiceImpl;
import com.oa.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 资产业务单元测试。
 * 使用 Mockito 隔离数据库，重点验证业务状态变化、异常分支和持久化调用。
 */
@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {
    @Mock private AssetMapper assetMapper;
    @Mock private AssetRecordMapper recordMapper;
    private AssetServiceImpl service;

    @BeforeAll
    static void initializeMyBatisMetadata() {
        // 纯单元测试不会启动 Spring/MyBatis，需要手动建立 LambdaWrapper 使用的字段缓存。
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "asset-unit-test");
        TableInfoHelper.initTableInfo(assistant, Asset.class);
        TableInfoHelper.initTableInfo(assistant, AssetRecord.class);
    }

    @BeforeEach
    void setUp() {
        service = new AssetServiceImpl(assetMapper, recordMapper);
    }

    @Test
    @DisplayName("登记资产时默认状态为可领用")
    void createAssetDefaultsToAvailable() {
        when(assetMapper.selectCount(any())).thenReturn(0L);
        AssetDTO dto = assetDto();

        service.create(dto);

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetMapper).insert(captor.capture());
        Asset saved = captor.getValue();
        assertEquals(1, saved.getStatus());
        assertEquals("PC-001", saved.getAssetCode());
        assertNotNull(saved.getCreateTime());
    }

    @Test
    @DisplayName("重复资产编码不能登记")
    void createRejectsDuplicateCode() {
        when(assetMapper.selectCount(any())).thenReturn(1L);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(assetDto()));
        assertEquals(60003, exception.getCode());
        verify(assetMapper, never()).insert(any(Asset.class));
    }

    @Test
    @DisplayName("新增资产不能由客户端指定非可领用状态")
    void createRejectsClientControlledStatus() {
        AssetDTO dto = assetDto(); dto.setStatus(2);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(dto));
        assertEquals(400, exception.getCode());
        verifyNoInteractions(assetMapper, recordMapper);
    }

    @Test
    @DisplayName("普通编辑接口不能改变资产业务状态")
    void updateRejectsStatusTransition() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, 2));
        AssetDTO dto = assetDto(); dto.setStatus(1);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(1L, dto));
        assertEquals(400, exception.getCode());
        verify(assetMapper, never()).updateById(any(Asset.class));
    }

    @Test
    @DisplayName("编辑不存在的资产返回业务异常")
    void updateRejectsMissingAsset() {
        when(assetMapper.selectById(99L)).thenReturn(null);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.update(99L, assetDto()));
        assertEquals(60001, exception.getCode());
    }

    @Test
    @DisplayName("领用中的资产不能报废")
    void scrapRejectsBorrowedAsset() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, 2));
        when(recordMapper.selectCount(any())).thenReturn(1L);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.scrap(1L));
        assertEquals(60002, exception.getCode());
        verify(assetMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("成功领用后写入领用中记录")
    void borrowCreatesActiveRecord() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, 1));
        when(assetMapper.update(isNull(), any())).thenReturn(1);
        BorrowDTO dto = borrowDto();

        AssetRecord result = service.borrow(dto);

        assertEquals(1, result.getStatus());
        assertEquals(1L, result.getAssetId());
        assertEquals(7L, result.getUserId());
        verify(recordMapper).insert(result);
    }

    @Test
    @DisplayName("条件更新失败时拒绝重复领用")
    void borrowRejectsUnavailableAsset() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, 1));
        when(assetMapper.update(isNull(), any())).thenReturn(0);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.borrow(borrowDto()));
        assertEquals(60002, exception.getCode());
        verify(recordMapper, never()).insert(any(AssetRecord.class));
    }

    @Test
    @DisplayName("预计归还日期不能早于领用日期")
    void borrowValidatesDates() {
        when(assetMapper.selectById(1L)).thenReturn(asset(1L, 1));
        when(assetMapper.update(isNull(), any())).thenReturn(1);
        BorrowDTO dto = borrowDto();
        dto.setBorrowDate(LocalDate.of(2026, 7, 21));
        dto.setExpectReturnDate(LocalDate.of(2026, 7, 20));
        BusinessException exception = assertThrows(BusinessException.class, () -> service.borrow(dto));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("归还资产同时更新记录和资产状态")
    void returnAssetUpdatesBothTables() {
        AssetRecord record = new AssetRecord();
        record.setId(10L); record.setAssetId(1L); record.setStatus(1);
        when(recordMapper.selectById(10L)).thenReturn(record);
        when(recordMapper.update(isNull(), any())).thenReturn(1);
        when(assetMapper.update(isNull(), any())).thenReturn(1);

        service.returnAsset(10L);

        verify(recordMapper).update(isNull(), any());
        verify(assetMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("同一领用记录不能重复归还")
    void returnAssetRejectsDuplicateReturn() {
        AssetRecord record = new AssetRecord();
        record.setId(10L); record.setAssetId(1L); record.setStatus(1);
        when(recordMapper.selectById(10L)).thenReturn(record);
        when(recordMapper.update(isNull(), any())).thenReturn(0);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.returnAsset(10L));
        assertEquals(60005, exception.getCode());
        verify(assetMapper, never()).update(isNull(), any());
    }

    private AssetDTO assetDto() {
        AssetDTO dto = new AssetDTO();
        dto.setAssetName("测试电脑"); dto.setAssetCode("PC-001"); dto.setCategory(3);
        dto.setPurchasePrice(new BigDecimal("5999.00")); dto.setPurchaseDate(LocalDate.of(2026, 7, 1));
        return dto;
    }
    private BorrowDTO borrowDto() {
        BorrowDTO dto = new BorrowDTO(); dto.setAssetId(1L); dto.setUserId(7L);
        dto.setBorrowDate(LocalDate.of(2026, 7, 21)); dto.setExpectReturnDate(LocalDate.of(2026, 12, 31));
        return dto;
    }
    private Asset asset(Long id, int status) {
        Asset asset = new Asset(); asset.setId(id); asset.setStatus(status); return asset;
    }
}
