package cn.badminton.dto.user;

import cn.badminton.model.User;

/**
 * 用户返回体（屏蔽敏感字段）
 * 作者: xiaolei
 */
public class UserResponse {
    private String id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.phone = u.getPhone();
        r.nickname = u.getNickname();
        r.avatar = u.getAvatar();
        r.status = u.getStatus();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

