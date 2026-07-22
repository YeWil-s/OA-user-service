package com.oa.asset.integration;

import com.oa.asset.dto.AssetDTO;
import com.oa.asset.dto.BorrowDTO;
import com.oa.asset.entity.Asset;
import com.oa.asset.service.AssetService;
import com.oa.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** 使用MySQL兼容模式数据库验证Mapper映射、事务回滚和并发状态一致性。 */
@SpringBootTest
@ActiveProfiles("test")
class AssetDatabaseIntegrationTest {
    @Autowired private AssetService assetService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM ast_asset_record");
        jdbcTemplate.update("DELETE FROM ast_asset");
    }

    @Test
    @DisplayName("真实Mapper能够完成资产写入和字段映射")
    void mapperPersistsAndReadsAsset() {
        assetService.create(assetDto("DB-001"));
        Asset asset = assetService.page(1, 10, "DB-001", 3, 1).getRecords().get(0);
        assertEquals("数据库测试资产", asset.getAssetName());
        assertEquals(new BigDecimal("1234.50"), asset.getPurchasePrice());
        assertEquals(1, asset.getStatus());
    }

    @Test
    @DisplayName("领用后发生日期异常时事务回滚资产状态")
    void borrowRollsBackWhenValidationFailsAfterUpdate() {
        assetService.create(assetDto("TX-001"));
        Long id = assetService.page(1, 10, "TX-001", null, null).getRecords().get(0).getId();
        BorrowDTO dto = new BorrowDTO(); dto.setAssetId(id); dto.setUserId(1L);
        dto.setBorrowDate(LocalDate.of(2026, 7, 22)); dto.setExpectReturnDate(LocalDate.of(2026, 7, 21));

        assertThrows(BusinessException.class, () -> assetService.borrow(dto));
        assertEquals(1, assetService.get(id).getStatus());
        assertEquals(0L, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ast_asset_record WHERE asset_id=?", Long.class, id));
    }

    @Test
    @DisplayName("领用与报废并发竞争后数据库状态保持一致")
    void borrowAndScrapRaceRemainsConsistent() throws Exception {
        assetService.create(assetDto("RACE-001"));
        Long id = assetService.page(1, 10, "RACE-001", null, null).getRecords().get(0).getId();
        BorrowDTO dto = new BorrowDTO(); dto.setAssetId(id); dto.setUserId(2L); dto.setBorrowDate(LocalDate.now());
        CountDownLatch ready = new CountDownLatch(2); CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> borrow = executor.submit(() -> runTogether(ready, start, () -> assetService.borrow(dto)));
        Future<Boolean> scrap = executor.submit(() -> runTogether(ready, start, () -> assetService.scrap(id)));
        ready.await(); start.countDown();
        boolean borrowSuccess = borrow.get(); boolean scrapSuccess = scrap.get(); executor.shutdownNow();

        assertNotEquals(borrowSuccess, scrapSuccess, "领用和报废必须恰好一个成功");
        int status = assetService.get(id).getStatus();
        long active = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ast_asset_record WHERE asset_id=? AND status=1", Long.class, id);
        if (borrowSuccess) { assertEquals(2, status); assertEquals(1L, active); }
        else { assertEquals(0, status); assertEquals(0L, active); }
    }

    private boolean runTogether(CountDownLatch ready, CountDownLatch start, ThrowingAction action) throws Exception {
        ready.countDown(); start.await();
        try { action.run(); return true; } catch (BusinessException e) { return false; }
    }
    private AssetDTO assetDto(String code) {
        AssetDTO dto = new AssetDTO(); dto.setAssetName("数据库测试资产"); dto.setAssetCode(code);
        dto.setCategory(3); dto.setPurchaseDate(LocalDate.of(2026, 7, 22)); dto.setPurchasePrice(new BigDecimal("1234.50"));
        return dto;
    }
    @FunctionalInterface private interface ThrowingAction { void run(); }
}
