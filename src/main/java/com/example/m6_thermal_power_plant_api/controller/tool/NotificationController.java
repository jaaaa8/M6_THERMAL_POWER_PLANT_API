package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.NotificationResponse;
import com.example.m6_thermal_power_plant_api.service.tool.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<NotificationResponse>> getByAccount(@PathVariable Integer accountId) {
        return ApiResponse.success(notificationService.getByAccount(accountId));
    }

    @GetMapping("/account/{accountId}/unread-count")
    public ApiResponse<Long> countUnread(@PathVariable Integer accountId) {
        return ApiResponse.success(notificationService.countUnread(accountId));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Integer id,
                                      @RequestParam Integer accountId) {
        notificationService.markRead(id, accountId);
        return ApiResponse.success("Đã đọc", null);
    }

    @PatchMapping("/account/{accountId}/read-all")
    public ApiResponse<Void> markAllRead(@PathVariable Integer accountId) {
        notificationService.markAllRead(accountId);
        return ApiResponse.success("Đã đọc tất cả", null);
    }
}
