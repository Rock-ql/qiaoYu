package cn.badminton.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 用户实体模型
 * Redis存储结构: badminton:user:{user_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class User extends BaseEntity {
    
    /**
     * 手机号（登录凭证）
     * 必须是11位中国手机号格式
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 用户昵称
     * 长度限制2-20个字符，不能包含特殊字符
     */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$", message = "昵称不能包含特殊字符")
    private String nickname;
    
    /**
     * 用户密码（加密存储）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    /**
     * 头像URL
     * 必须是有效的URL格式
     */
    private String avatar = "";
    
    /**
     * 用户状态：1-正常，2-禁用
     */
    @NotNull(message = "用户状态不能为空")
    @Min(value = 1, message = "用户状态值无效")
    @Max(value = 2, message = "用户状态值无效")
    private Integer status = 1;
    
    /**
     * 参与活动总数
     */
    @Min(value = 0, message = "参与活动总数不能为负数")
    private Integer totalActivities = 0;
    
    /**
     * 总消费金额
     * 使用BigDecimal确保精度
     */
    @DecimalMin(value = "0.00", message = "总消费金额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "总消费金额格式不正确")
    private BigDecimal totalExpense = BigDecimal.ZERO;
    
    /**
     * 微信OpenID（用于微信登录）
     */
    private String wxOpenId = "";
    
    /**
     * 微信UnionID（用于跨应用用户识别）
     */
    private String wxUnionId = "";

    public User() {
        super();
    }

    public User(String phone, String nickname, String password) {
        super();
        this.phone = phone;
        this.nickname = nickname;
        this.password = password;
    }

    /**
     * 简单有效性校验
     * 用于服务层快速判断对象是否满足最基本约束
     */
    public boolean isValid() {
        boolean phoneOk = this.phone != null && this.phone.matches("^1[3-9]\\\\d{9}$");
        boolean nicknameOk = this.nickname != null && this.nickname.trim().length() >= 2;
        boolean passwordOk = this.password != null && this.password.length() >= 6;
        boolean statusOk = this.status != null && (this.status == 1 || this.status == 2);
        return phoneOk && nicknameOk && passwordOk && statusOk;
    }

    // Getter和Setter方法
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? "" : avatar;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getTotalActivities() {
        return totalActivities;
    }
    
    public void setTotalActivities(Integer totalActivities) {
        this.totalActivities = totalActivities;
    }
    
    public BigDecimal getTotalExpense() {
        return totalExpense;
    }
    
    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }
    
    public String getWxOpenId() {
        return wxOpenId;
    }
    
    public void setWxOpenId(String wxOpenId) {
        this.wxOpenId = wxOpenId == null ? "" : wxOpenId;
    }
    
    public String getWxUnionId() {
        return wxUnionId;
    }
    
    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId == null ? "" : wxUnionId;
    }
    
    /**
     * 增加活动参与次数
     */
    public void incrementActivities() {
        this.totalActivities++;
        updateTimestamp();
    }
    
    /**
     * 减少活动参与次数
     */
    public void decrementActivities() {
        if (this.totalActivities > 0) {
            this.totalActivities--;
            updateTimestamp();
        }
    }
    
    /**
     * 增加消费金额
     * @param amount 消费金额
     */
    public void addExpense(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalExpense = this.totalExpense.add(amount);
            updateTimestamp();
        }
    }
    
    /**
     * 减少消费金额
     * @param amount 减少金额
     */
    public void subtractExpense(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newExpense = this.totalExpense.subtract(amount);
            if (newExpense.compareTo(BigDecimal.ZERO) >= 0) {
                this.totalExpense = newExpense;
                updateTimestamp();
            }
        }
    }
    
    /**
     * 检查用户是否可用
     */
    public boolean isActive() {
        return this.status == 1 && !isDeleted();
    }
    
    /**
     * 禁用用户
     */
    public void disable() {
        this.status = 2;
        updateTimestamp();
    }
    
    /**
     * 启用用户
     */
    public void enable() {
        this.status = 1;
        updateTimestamp();
    }
    
    /**
     * 绑定微信
     */
    public void bindWechat(String openId, String unionId) {
        this.wxOpenId = openId == null ? "" : openId;
        this.wxUnionId = unionId == null ? "" : unionId;
        updateTimestamp();
    }
    
    /**
     * 解绑微信
     */
    public void unbindWechat() {
        this.wxOpenId = "";
        this.wxUnionId = "";
        updateTimestamp();
    }
    
    /**
     * 检查是否绑定微信
     */
    public boolean isWechatBound() {
        return wxOpenId != null && !wxOpenId.isEmpty();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", totalActivities=" + totalActivities +
                ", totalExpense=" + totalExpense +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
