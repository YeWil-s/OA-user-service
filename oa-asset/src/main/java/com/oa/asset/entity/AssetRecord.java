package com.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("ast_asset_record")
/** 资产领用和归还记录，对应 ast_asset_record 表。 */
public class AssetRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long userId;
    private LocalDate borrowDate;
    private LocalDate expectReturnDate;
    private LocalDate actualReturnDate;
    private Integer status;
    private LocalDateTime createTime;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getAssetId() { return assetId; } public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getBorrowDate() { return borrowDate; } public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getExpectReturnDate() { return expectReturnDate; } public void setExpectReturnDate(LocalDate expectReturnDate) { this.expectReturnDate = expectReturnDate; }
    public LocalDate getActualReturnDate() { return actualReturnDate; } public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
