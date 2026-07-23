package com.oa.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.asset.dto.AssetDTO;
import com.oa.asset.dto.BorrowDTO;
import com.oa.asset.entity.Asset;
import com.oa.asset.entity.AssetRecord;
import com.oa.asset.service.AssetService;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "资产管理")
@RestController
@RequestMapping("/api/asset")
@Validated
/**
 * 资产接口：提供资产登记、查询、报废、领用、归还及领用记录查询能力。
 */
public class AssetController {
    private final AssetService service;
    public AssetController(AssetService service) { this.service = service; }

    @Operation(summary = "资产分页列表")
    @GetMapping("/assets")
    public Result<IPage<Asset>> list(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer status) {
        return Result.success(service.page(pageNum, pageSize, keyword, category, status));
    }
    @GetMapping("/assets/{id}") public Result<Asset> detail(@PathVariable @Positive Long id) { return Result.success(service.get(id)); }
    @PostMapping("/assets") public Result<Void> create(@Valid @RequestBody AssetDTO dto) { service.create(dto); return Result.success(); }
    @PutMapping("/assets/{id}") public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AssetDTO dto) { service.update(id, dto); return Result.success(); }
    @DeleteMapping("/assets/{id}") public Result<Void> scrap(@PathVariable Long id) { service.scrap(id); return Result.success(); }
    @PostMapping("/borrow") public Result<AssetRecord> borrow(@Valid @RequestBody BorrowDTO dto) { return Result.success(service.borrow(dto)); }
    @PutMapping("/borrow/{id}/return") public Result<Void> returnAsset(@PathVariable Long id) { service.returnAsset(id); return Result.success(); }
    @PostMapping("/internal/borrow") public Result<AssetRecord> borrowInternal(@Valid @RequestBody BorrowDTO dto) { return Result.success(service.borrow(dto)); }
    @GetMapping("/records")
    public Result<IPage<AssetRecord>> records(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) Long assetId, @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success(service.records(pageNum, pageSize, assetId, userId, status));
    }
}
