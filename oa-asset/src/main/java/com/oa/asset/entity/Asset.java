package com.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("ast_asset")
/** 资产主数据，对应 ast_asset 表。 */
public class Asset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String assetName;
    private String assetCode;
    private Integer category;
    private String model;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getAssetName() { return assetName; } public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getAssetCode() { return assetCode; } public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public Integer getCategory() { return category; } public void setCategory(Integer category) { this.category = category; }
    public String getModel() { return model; } public void setModel(String model) { this.model = model; }
    public LocalDate getPurchaseDate() { return purchaseDate; } public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public BigDecimal getPurchasePrice() { return purchasePrice; } public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; } public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
