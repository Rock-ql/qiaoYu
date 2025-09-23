package cn.badminton.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信授权登录请求
 * 作者: xiaolei
 */
public class WechatLoginRequest {
    // 公众号/网页授权 code
    @NotBlank
    private String code;

    // 小程序可选参数
    private String encryptedData = "";
    private String iv = "";

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getEncryptedData() { return encryptedData; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData == null ? "" : encryptedData; }
    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv == null ? "" : iv; }
}

