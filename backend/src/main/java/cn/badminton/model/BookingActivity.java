package cn.badminton.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 约球活动实体模型
 * MySQL存储 + Redis缓存
 *
 * 作者: xiaolei
 */
@Entity
@Table(name = "booking_activity",
       indexes = {
           @Index(name = "idx_organizer", columnList = "organizer"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_venue", columnList = "venue"),
           @Index(name = "idx_start_time", columnList = "start_time"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class BookingActivity extends BaseEntity {
    
    /**
     * 活动标题
     * 长度限制5-50个字符
     */
    @Column(name = "title", length = 100, nullable = false)
    @NotBlank(message = "活动标题不能为空")
    @Size(min = 5, max = 50, message = "活动标题长度必须在5-50个字符之间")
    private String title;

    /**
     * 发起人用户ID
     */
    @Column(name = "organizer", length = 36, nullable = false)
    @NotBlank(message = "发起人不能为空")
    private String organizer;

    /**
     * 场地名称
     */
    @Column(name = "venue", length = 100, nullable = false)
    @NotBlank(message = "场地名称不能为空")
    @Size(max = 100, message = "场地名称长度不能超过100个字符")
    private String venue;

    /**
     * 详细地址
     */
    @Column(name = "address", length = 200, nullable = false)
    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    private String address = "";

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time", nullable = false)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 最大人数
     * 限制2-20人
     */
    @Column(name = "max_players", nullable = false)
    @NotNull(message = "最大人数不能为空")
    @Min(value = 2, message = "最大人数不能少于2人")
    @Max(value = 20, message = "最大人数不能超过20人")
    private Integer maxPlayers;

    /**
     * 当前人数
     */
    @Column(name = "current_players", nullable = false)
    @Min(value = 0, message = "当前人数不能为负数")
    private Integer currentPlayers = 1;

    /**
     * 预估费用
     * 非负数，最多2位小数
     */
    @Column(name = "fee", nullable = false, precision = 8, scale = 2)
    @DecimalMin(value = "0.00", message = "预估费用不能为负数")
    @Digits(integer = 8, fraction = 2, message = "预估费用格式不正确")
    private BigDecimal fee = BigDecimal.ZERO;

    /**
     * 活动描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 500, message = "活动描述长度不能超过500个字符")
    private String description = "";

    /**
     * 活动状态：1-待确认 2-进行中 3-已完成 4-已取消
     */
    @Column(name = "status", nullable = false)
    @NotNull(message = "活动状态不能为空")
    @Min(value = 1, message = "活动状态值无效")
    @Max(value = 4, message = "活动状态值无效")
    private Integer status = STATUS_PENDING;

    // 状态常量
    public static final int STATUS_PENDING = 1;     // 待确认
    public static final int STATUS_ONGOING = 2;     // 进行中
    public static final int STATUS_COMPLETED = 3;   // 已完成
    public static final int STATUS_CANCELLED = 4;   // 已取消

    public BookingActivity() {
        super();
    }

    public BookingActivity(String title, String organizer, String venue, 
                          LocalDateTime startTime, LocalDateTime endTime, Integer maxPlayers) {
        super();
        this.title = title;
        this.organizer = organizer;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPlayers = maxPlayers;
    }

    // Getter和Setter方法

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

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
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
        updateTimestamp();
    }

    /**
     * 增加参与人数
     */
    public boolean addPlayer() {
        if (currentPlayers < maxPlayers) {
            this.currentPlayers++;
            updateTimestamp();
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
            updateTimestamp();
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
     * 验证结束时间是否晚于开始时间
     */
    @AssertTrue(message = "结束时间必须晚于开始时间")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // 让@NotNull验证处理
        }
        return endTime.isAfter(startTime);
    }
    
    /**
     * 开始活动
     */
    public void startActivity() {
        if (this.status == STATUS_PENDING) {
            this.status = STATUS_ONGOING;
            updateTimestamp();
        }
    }
    
    /**
     * 完成活动
     */
    public void completeActivity() {
        if (this.status == STATUS_ONGOING) {
            this.status = STATUS_COMPLETED;
            updateTimestamp();
        }
    }
    
    /**
     * 取消活动
     */
    public void cancelActivity() {
        if (canCancel()) {
            this.status = STATUS_CANCELLED;
            updateTimestamp();
        }
    }
    
    /**
     * 检查活动是否已过期
     */
    public boolean isExpired() {
        return endTime != null && endTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * 检查活动是否即将开始（1小时内）
     */
    public boolean isStartingSoon() {
        if (startTime == null) return false;
        LocalDateTime oneHourLater = LocalDateTime.now().plusHours(1);
        return startTime.isBefore(oneHourLater) && startTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * 获取剩余参与名额
     */
    public int getAvailableSlots() {
        return maxPlayers - currentPlayers;
    }

    /**
     * 简单有效性校验
     * 用于服务层快速判断对象是否满足最基本约束
     */
    public boolean isValid() {
        boolean titleOk = this.title != null && !this.title.trim().isEmpty();
        boolean organizerOk = this.organizer != null && !this.organizer.trim().isEmpty();
        boolean venueOk = this.venue != null && !this.venue.trim().isEmpty();
        boolean timeOk = this.startTime != null && this.endTime != null && this.endTime.isAfter(this.startTime);
        boolean maxPlayersOk = this.maxPlayers != null && this.maxPlayers >= 2 && this.maxPlayers <= 20;
        return titleOk && organizerOk && venueOk && timeOk && maxPlayersOk;
    }

    @Override
    public String toString() {
        return "BookingActivity{" +
                "id='" + getId() + '\'' +
                ", title='" + title + '\'' +
                ", organizer='" + organizer + '\'' +
                ", venue='" + venue + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", currentPlayers=" + currentPlayers +
                ", maxPlayers=" + maxPlayers +
                ", status=" + status +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
