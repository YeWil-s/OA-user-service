package com.oa.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.asset.dto.AssetDTO;
import com.oa.asset.dto.BorrowDTO;
import com.oa.asset.entity.Asset;
import com.oa.asset.entity.AssetRecord;

public interface AssetService {
    IPage<Asset> page(int pageNum, int pageSize, String keyword, Integer category, Integer status);
    Asset get(Long id);
    void create(AssetDTO dto);
    void update(Long id, AssetDTO dto);
    void scrap(Long id);
    AssetRecord borrow(BorrowDTO dto);
    void returnAsset(Long recordId);
    IPage<AssetRecord> records(int pageNum, int pageSize, Long assetId, Long userId, Integer status);
}
