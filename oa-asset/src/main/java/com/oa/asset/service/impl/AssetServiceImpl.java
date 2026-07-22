package com.oa.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.asset.dto.AssetDTO;
import com.oa.asset.dto.BorrowDTO;
import com.oa.asset.entity.Asset;
import com.oa.asset.entity.AssetRecord;
import com.oa.asset.mapper.AssetMapper;
import com.oa.asset.mapper.AssetRecordMapper;
import com.oa.asset.service.AssetService;
import com.oa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
/**
 * 资产业务实现。
 *
 * <p>资产状态约定：0=已报废，1=可领用，2=已领用；
 * 领用记录状态约定：1=领用中，2=已归还。</p>
 */
public class AssetServiceImpl implements AssetService {
    private static final int NOT_FOUND = 60001;
    private static final int UNAVAILABLE = 60002;
    private static final int CODE_DUPLICATE = 60003;
    private static final int RECORD_NOT_FOUND = 60004;
    private static final int ALREADY_RETURNED = 60005;
    private final AssetMapper assetMapper;
    private final AssetRecordMapper recordMapper;

    public AssetServiceImpl(AssetMapper assetMapper, AssetRecordMapper recordMapper) {
        this.assetMapper = assetMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public IPage<Asset> page(int pageNum, int pageSize, String keyword, Integer category, Integer status) {
        LambdaQueryWrapper<Asset> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) query.and(w -> w.like(Asset::getAssetName, keyword).or().like(Asset::getAssetCode, keyword));
        if (category != null) query.eq(Asset::getCategory, category);
        if (status != null) query.eq(Asset::getStatus, status);
        return assetMapper.selectPage(new Page<>(pageNum, pageSize), query.orderByDesc(Asset::getCreateTime));
    }

    @Override public Asset get(Long id) { return requireAsset(id); }

    @Override
    public void create(AssetDTO dto) {
        if (dto.getStatus() != null && dto.getStatus() != 1) {
            throw new BusinessException(400, "新登记资产的状态只能为可领用");
        }
        ensureCodeUnique(dto.getAssetCode(), null);
        Asset asset = new Asset();
        copy(dto, asset);
        asset.setStatus(1);
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        assetMapper.insert(asset);
    }

    @Override
    public void update(Long id, AssetDTO dto) {
        Asset asset = requireAsset(id);
        if (dto.getStatus() != null && !dto.getStatus().equals(asset.getStatus())) {
            throw new BusinessException(400, "资产状态只能通过领用、归还或报废操作修改");
        }
        ensureCodeUnique(dto.getAssetCode(), id);
        copy(dto, asset);
        asset.setUpdateTime(LocalDateTime.now());
        assetMapper.updateById(asset);
    }

    @Override
    public void scrap(Long id) {
        requireAsset(id);
        Long active = recordMapper.selectCount(new LambdaQueryWrapper<AssetRecord>().eq(AssetRecord::getAssetId, id).eq(AssetRecord::getStatus, 1));
        if (active > 0) throw new BusinessException(UNAVAILABLE, "资产领用中，归还后才能报废");
        // 报废也使用状态条件更新，与领用竞争时只有一个操作能成功，避免出现“已报废但仍领用中”。
        int updated = assetMapper.update(null, new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getId, id).eq(Asset::getStatus, 1)
                .set(Asset::getStatus, 0).set(Asset::getUpdateTime, LocalDateTime.now()));
        if (updated != 1) throw new BusinessException(UNAVAILABLE, "资产当前状态不可报废");
    }

    @Override
    @Transactional
    public AssetRecord borrow(BorrowDTO dto) {
        requireAsset(dto.getAssetId());
        // 将“检查状态”和“修改状态”合并为一条条件 UPDATE。
        // 并发请求中只有一个请求能把状态从1改为2，从而避免同一资产被重复领用。
        int updated = assetMapper.update(null, new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getId, dto.getAssetId()).eq(Asset::getStatus, 1)
                .set(Asset::getStatus, 2).set(Asset::getUpdateTime, LocalDateTime.now()));
        if (updated != 1) throw new BusinessException(UNAVAILABLE, "资产当前不可领用");
        LocalDate borrowDate = dto.getBorrowDate() == null ? LocalDate.now() : dto.getBorrowDate();
        if (dto.getExpectReturnDate() != null && dto.getExpectReturnDate().isBefore(borrowDate)) {
            throw new BusinessException(400, "预计归还日期不能早于领用日期");
        }
        AssetRecord record = new AssetRecord();
        record.setAssetId(dto.getAssetId()); record.setUserId(dto.getUserId());
        record.setBorrowDate(borrowDate); record.setExpectReturnDate(dto.getExpectReturnDate());
        record.setStatus(1); record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public void returnAsset(Long recordId) {
        AssetRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(RECORD_NOT_FOUND, "领用记录不存在");
        // 只有状态为“领用中”的记录可以归还，条件更新同时避免重复归还。
        int updated = recordMapper.update(null, new LambdaUpdateWrapper<AssetRecord>()
                .eq(AssetRecord::getId, recordId).eq(AssetRecord::getStatus, 1)
                .set(AssetRecord::getStatus, 2).set(AssetRecord::getActualReturnDate, LocalDate.now()));
        if (updated != 1) throw new BusinessException(ALREADY_RETURNED, "该领用记录已归还");
        int assetUpdated = assetMapper.update(null, new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getId, record.getAssetId()).eq(Asset::getStatus, 2)
                .set(Asset::getStatus, 1).set(Asset::getUpdateTime, LocalDateTime.now()));
        if (assetUpdated != 1) throw new BusinessException(UNAVAILABLE, "资产状态异常，归还失败");
    }

    @Override
    public IPage<AssetRecord> records(int pageNum, int pageSize, Long assetId, Long userId, Integer status) {
        LambdaQueryWrapper<AssetRecord> query = new LambdaQueryWrapper<>();
        if (assetId != null) query.eq(AssetRecord::getAssetId, assetId);
        if (userId != null) query.eq(AssetRecord::getUserId, userId);
        if (status != null) query.eq(AssetRecord::getStatus, status);
        return recordMapper.selectPage(new Page<>(pageNum, pageSize), query.orderByDesc(AssetRecord::getCreateTime));
    }

    private Asset requireAsset(Long id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) throw new BusinessException(NOT_FOUND, "资产不存在");
        return asset;
    }
    private void ensureCodeUnique(String code, Long exceptId) {
        // 编辑资产时排除当前资产自身，否则保留原编码也会被误判为重复。
        LambdaQueryWrapper<Asset> query = new LambdaQueryWrapper<Asset>().eq(Asset::getAssetCode, code);
        if (exceptId != null) query.ne(Asset::getId, exceptId);
        if (assetMapper.selectCount(query) > 0) throw new BusinessException(CODE_DUPLICATE, "资产编码已存在");
    }
    private void copy(AssetDTO dto, Asset asset) {
        if (dto.getCategory() < 1 || dto.getCategory() > 3) throw new BusinessException(400, "资产分类仅支持1-3");
        asset.setAssetName(dto.getAssetName()); asset.setAssetCode(dto.getAssetCode()); asset.setCategory(dto.getCategory());
        asset.setModel(dto.getModel()); asset.setPurchaseDate(dto.getPurchaseDate()); asset.setPurchasePrice(dto.getPurchasePrice());
    }
}
