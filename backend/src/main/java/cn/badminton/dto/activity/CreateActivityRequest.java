package cn.badminton.dto.activity;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 创建活动请求
 * 作者: xiaolei
 */
public class CreateActivityRequest {
    @NotBlank
    private String organizerId;

    @NotBlank
    @Size(min = 5, max = 50)
    private String title;

    @NotBlank
    private String venue;

    @Future
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    @Min(2)
    @Max(20)
    private Integer maxPlayers;

    @Size(max = 500)
    private String description = "";

    private String address = "";

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address == null ? "" : address; }
}

