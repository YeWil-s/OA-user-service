package com.oa.asset.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetDTO {
    @NotBlank(message = "资产名称不能为空")
    private String assetName;
    @NotBlank(message = "资产编码不能为空")
    private String assetCode;
    @NotNull(message = "资产分类不能为空")
    @Min(value = 1, message = "资产分类仅支持1-3")
    @Max(value = 3, message = "资产分类仅支持1-3")
    private Integer category;
    private String model;
    private LocalDate purchaseDate;
    @DecimalMin(value = "0.00", message = "购置价格不能小于0")
    private BigDecimal purchasePrice;
    @Min(value = 0, message = "资产状态仅支持0-2")
    @Max(value = 2, message = "资产状态仅支持0-2")
    private Integer status;
    public String getAssetName() { return assetName; } public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getAssetCode() { return assetCode; } public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public Integer getCategory() { return category; } public void setCategory(Integer category) { this.category = category; }
    public String getModel() { return model; } public void setModel(String model) { this.model = model; }
    public LocalDate getPurchaseDate() { return purchaseDate; } public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public BigDecimal getPurchasePrice() { return purchasePrice; } public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
