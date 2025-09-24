package cn.badminton.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体的公共字段
 *
 * @author xiaolei
 */
@MappedSuperclass
public abstract class BaseEntity {
    
    /**
     * 主键ID
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 租户ID，用于支持私有部署
     */
    @Column(name = "tenant", nullable = false)
    private Integer tenant = 1;

    /**
     * 状态：0-未知，1-上架，2-下架
     */
    @Column(name = "state", nullable = false)
    private Integer state = 1;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 组织ID
     */
    @Column(name = "organization_id")
    private Integer organizationId;
    
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Integer getTenant() {
        return tenant;
    }
    
    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state;
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public Integer getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }
    
    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.state = 2; // 设置为下架状态
    }
    
    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}