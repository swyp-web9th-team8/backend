package com.swyp.plogging.backend.notification.controller;

import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.notification.domain.AppNotification;
import com.swyp.plogging.backend.notification.service.NotificationService;
import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.notification.strategy.NotificationStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "현재는 테스트용으로 작성되어 있습니다.")
public class NotificationController {
    private final NotificationStrategy notificationStrategy;

    record NotificationRequest(@Schema(description = "알림 제목, 문자열") String title,
                               @Schema(description = "메세지 내용, 문자열") String body) {
    }

    @Operation(summary = "FCM Test를 위한 Endpoint",
            description = """
                    FCM Test를 위한 Post Method Endpoint입니다.
                    유저 로그인 및 해당 유저가 FCM 토큰을 가져야만 합니다. 그렇지 않으면 에러가 납니다.
                    해당 테스트는 알림을 DB에 저장하지는 않습니다.""")
    @PostMapping("/test/send")
    public ApiResponse<Object> sendNotificationTest(@AuthenticationPrincipal OAuth2User oAuth2User,
                                                    @RequestBody NotificationRequest request) {
        try {
            // FCM 선택
            NotificationService service = notificationStrategy.getService(NotiStrategy.FCM);
            // 알림을 위한 Noti 생성
            AppNotification notification = AppNotification.newInstance(request.title, request.body, SecurityUtils.getUserOrThrow(oAuth2User));
            service.notify(notification);
            return ApiResponse.ok(null, "테스트 알림 보내기를 성공했습니다.");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
