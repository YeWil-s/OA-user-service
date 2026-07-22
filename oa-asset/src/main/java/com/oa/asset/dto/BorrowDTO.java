package com.oa.asset.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class BorrowDTO {
    @NotNull(message = "资产ID不能为空") @Positive(message = "资产ID必须为正数") private Long assetId;
    @NotNull(message = "领用人ID不能为空") @Positive(message = "领用人ID必须为正数") private Long userId;
    private LocalDate borrowDate;
    private LocalDate expectReturnDate;
    public Long getAssetId() { return assetId; } public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getBorrowDate() { return borrowDate; } public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getExpectReturnDate() { return expectReturnDate; } public void setExpectReturnDate(LocalDate expectReturnDate) { this.expectReturnDate = expectReturnDate; }
}
