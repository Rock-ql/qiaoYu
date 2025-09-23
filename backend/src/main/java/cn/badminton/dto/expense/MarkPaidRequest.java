package cn.badminton.dto.expense;

import jakarta.validation.constraints.NotBlank;

/**
 * 标记已支付请求
 * 作者: xiaolei
 */
public class MarkPaidRequest {
    @NotBlank
    private String shareId;

    @NotBlank
    private String userId;

    public String getShareId() { return shareId; }
    public void setShareId(String shareId) { this.shareId = shareId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

