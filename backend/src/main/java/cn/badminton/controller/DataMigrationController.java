package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.migration.DataMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据迁移控制器
 * 提供数据迁移相关的API接口
 *
 * 作者: xiaolei
 */
@RestController
@Slf4j
@RequestMapping("/api/migration")
public class DataMigrationController {

    @Autowired
    private DataMigrationService dataMigrationService;

    /**
     * 执行数据迁移
     */
    @PostMapping("/migrate")
    public Result<String> migrateData() {
        try {
            log.info("开始执行数据迁移...");
            dataMigrationService.migrateAllData();
            log.info("数据迁移完成");
            return Result.ok("数据迁移完成");
        } catch (Exception e) {
            log.error("数据迁移失败: {}", e.getMessage(), e);
            return Result.fail(500, "数据迁移失败: " + e.getMessage());
        }
    }
}