package cn.badminton.model;

import java.time.LocalDateTime;

/**
 * 约球活动实体模型
 * Redis存储结构: badminton:activity:{activity_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class BookingActivity {
    
    private String id;                  // 活动唯一标识
    private String title;               // 活动标题
    private String organizer;           // 发起人用户ID
    private String venue;               // 场地名称
    private String address;             // 详细地址
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private Integer maxPlayers;         // 最大人数
    private Integer currentPlayers;     // 当前人数
    private Double fee;                 // 预估费用
    private String description;         // 活动描述
    private Integer status;             // 状态: 1-待确认 2-进行中 3-已完成 4-已取消
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    // 状态常量
    public static final int STATUS_PENDING = 1;     // 待确认
    public static final int STATUS_ONGOING = 2;     // 进行中
    public static final int STATUS_COMPLETED = 3;   // 已完成
    public static final int STATUS_CANCELLED = 4;   // 已取消

    // 默认构造函数
    public BookingActivity() {
        this.status = STATUS_PENDING;
        this.currentPlayers = 1; // 发起人默认参加
        this.fee = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 带参构造函数
    public BookingActivity(String title, String organizer, String venue, 
                          LocalDateTime startTime, LocalDateTime endTime, Integer maxPlayers) {
        this();
        this.title = title;
        this.organizer = organizer;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPlayers = maxPlayers;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(Integer currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
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

    /**
     * 增加参与人数
     */
    public boolean addPlayer() {
        if (currentPlayers < maxPlayers) {
            this.currentPlayers++;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 减少参与人数
     */
    public boolean removePlayer() {
        if (currentPlayers > 0) {
            this.currentPlayers--;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 检查活动是否已满
     */
    public boolean isFull() {
        return currentPlayers >= maxPlayers;
    }

    /**
     * 检查活动是否可以参加
     */
    public boolean canJoin() {
        return status == STATUS_PENDING && !isFull();
    }

    /**
     * 检查活动是否可以取消
     */
    public boolean canCancel() {
        return status == STATUS_PENDING || status == STATUS_ONGOING;
    }

    /**
     * 验证活动数据
     */
    public boolean isValid() {
        return title != null && !title.isEmpty()
            && organizer != null && !organizer.isEmpty()
            && venue != null && !venue.isEmpty()
            && startTime != null && endTime != null
            && maxPlayers != null && maxPlayers > 0
            && startTime.isBefore(endTime);
    }

    @Override
    public String toString() {
        return "BookingActivity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", organizer='" + organizer + '\'' +
                ", venue='" + venue + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", currentPlayers=" + currentPlayers +
                ", maxPlayers=" + maxPlayers +
                ", status=" + status +
                '}';
    }
}