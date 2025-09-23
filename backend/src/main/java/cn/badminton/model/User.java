package cn.badminton.model;

import java.time.LocalDateTime;

/**
 * 用户实体模型
 * Redis存储结构: badminton:user:{user_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class User {
    
    private String id;               // 用户唯一标识
    private String phone;            // 手机号(登录凭证)
    private String nickname;         // 用户昵称
    private String avatar;           // 头像URL
    private String password;         // 密码(加密存储)
    private Integer status;          // 状态: 1-正常 2-禁用
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private Integer totalActivities; // 参与活动总数
    private Double totalExpense;     // 总消费金额
    private String wxOpenId;         // 微信OpenID
    private String wxUnionId;        // 微信UnionID

    // 默认构造函数
    public User() {
        this.status = 1; // 默认正常状态
        this.totalActivities = 0;
        this.totalExpense = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 带参构造函数
    public User(String phone, String nickname, String password) {
        this();
        this.phone = phone;
        this.nickname = nickname;
        this.password = password;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Integer totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(Double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public String getWxOpenId() {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId) {
        this.wxOpenId = wxOpenId;
    }

    public String getWxUnionId() {
        return wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
    }

    /**
     * 更新用户活动统计
     */
    public void incrementActivities() {
        this.totalActivities++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加用户消费金额
     */
    public void addExpense(Double amount) {
        if (amount != null && amount > 0) {
            this.totalExpense += amount;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 验证用户数据
     */
    public boolean isValid() {
        return phone != null && !phone.isEmpty() 
            && nickname != null && !nickname.isEmpty()
            && password != null && !password.isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", totalActivities=" + totalActivities +
                ", totalExpense=" + totalExpense +
                ", createdAt=" + createdAt +
                '}';
    }
}