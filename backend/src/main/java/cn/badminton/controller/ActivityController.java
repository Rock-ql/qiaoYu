package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.dto.activity.ActivitiesByStatusRequest;
import cn.badminton.dto.activity.ActivitiesByTimeRangeRequest;
import cn.badminton.dto.activity.CreateActivityRequest;
import cn.badminton.dto.activity.JoinActivityRequest;
import cn.badminton.dto.common.ActivityIdRequest;
import cn.badminton.dto.common.UserIdRequest;
import cn.badminton.model.BookingActivity;
import cn.badminton.service.ActivityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动控制器
 * 作者: xiaolei
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;

    @PostMapping("/create")
    public Result<BookingActivity> create(@Valid @RequestBody CreateActivityRequest req) {
        log.info("[Activity] 创建入参: organizerId={}, title={}", req.getOrganizerId(), req.getTitle());
        BookingActivity act = activityService.createActivity(
                req.getOrganizerId(), req.getTitle(), req.getVenue(),
                req.getStartTime(), req.getEndTime(), req.getMaxPlayers(),
                req.getDescription(), req.getAddress()
        );
        return Result.ok(act);
    }

    @PostMapping("/join")
    public Result<Boolean> join(@Valid @RequestBody JoinActivityRequest req) {
        log.info("[Activity] 参加入参: activityId={}, userId={}", req.getActivityId(), req.getUserId());
        boolean ok = activityService.joinActivity(req.getActivityId(), req.getUserId(), req.getRemark());
        return Result.ok(ok);
    }

    @PostMapping("/leave")
    public Result<Boolean> leave(@Valid @RequestBody JoinActivityRequest req) {
        log.info("[Activity] 退出入参: activityId={}, userId={}", req.getActivityId(), req.getUserId());
        boolean ok = activityService.leaveActivity(req.getActivityId(), req.getUserId());
        return Result.ok(ok);
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody JoinActivityRequest req) {
        log.info("[Activity] 取消入参: activityId={}, userId={}", req.getActivityId(), req.getUserId());
        activityService.cancelActivity(req.getActivityId(), req.getUserId());
        return Result.ok();
    }

    @PostMapping("/start")
    public Result<Void> start(@Valid @RequestBody JoinActivityRequest req) {
        log.info("[Activity] 开始入参: activityId={}, userId={}", req.getActivityId(), req.getUserId());
        activityService.startActivity(req.getActivityId(), req.getUserId());
        return Result.ok();
    }

    @PostMapping("/complete")
    public Result<Void> complete(@Valid @RequestBody JoinActivityRequest req) {
        log.info("[Activity] 完成入参: activityId={}, userId={}", req.getActivityId(), req.getUserId());
        activityService.completeActivity(req.getActivityId(), req.getUserId());
        return Result.ok();
    }

    @PostMapping("/detail")
    public Result<BookingActivity> detail(@Valid @RequestBody ActivityIdRequest req) {
        BookingActivity act = activityService.getActivityById(req.getActivityId());
        return Result.ok(act);
    }

    @PostMapping("/available")
    public Result<List<BookingActivity>> available() {
        return Result.ok(activityService.getAvailableActivities());
    }

    @PostMapping("/byStatus")
    public Result<List<BookingActivity>> byStatus(@Valid @RequestBody ActivitiesByStatusRequest req) {
        return Result.ok(activityService.getActivitiesByStatus(req.getStatus()));
    }

    @PostMapping("/byOrganizer")
    public Result<List<BookingActivity>> byOrganizer(@Valid @RequestBody UserIdRequest req) {
        return Result.ok(activityService.getUserActivities(req.getUserId()));
    }

    @PostMapping("/byTimeRange")
    public Result<List<BookingActivity>> byTimeRange(@Valid @RequestBody ActivitiesByTimeRangeRequest req) {
        return Result.ok(activityService.getActivitiesByTimeRange(req.getStartTime(), req.getEndTime()));
    }
}

