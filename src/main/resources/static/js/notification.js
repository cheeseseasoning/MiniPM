(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {

        const notificationButton =
                document.getElementById("notificationButton");

        const notificationBadge =
                document.getElementById("notificationBadge");

        const notificationSummary =
                document.getElementById("notificationSummary");

        const notificationList =
                document.getElementById("notificationList");

        const readAllButton =
                document.getElementById("readAllNotificationButton");

        // 로그인 화면처럼 공통 헤더가 없는 화면에서는 알림 코드를 실행하지 않음
        if (!notificationButton
                || !notificationBadge
                || !notificationSummary
                || !notificationList
                || !readAllButton) {

            return;
        }

        const csrfTokenElement =
                document.querySelector('meta[name="_csrf"]');

        const csrfHeaderElement =
                document.querySelector('meta[name="_csrf_header"]');

        let notifications = [];
        let unreadCount = 0;
        let stompClient = null;
        let reconnectTimer = null;

        loadNotifications()
                .finally(function () {
                    connectWebSocket();
                });

        readAllButton.addEventListener("click", function () {
            readAllNotifications();
        });

        // 기존 알림 목록과 읽지 않은 알림 개수를 함께 조회
        async function loadNotifications() {

            try {
                const results = await Promise.all([
                    requestJson("/api/notifications"),
                    requestJson("/api/notifications/unread-count")
                ]);

                notifications = results[0];
                unreadCount = results[1].unreadCount;

                renderNotifications();
                updateUnreadCount();

            } catch (error) {
                showNotificationError(
                        "알림을 불러오지 못했습니다."
                );
            }
        }

        // fetch 요청을 보내고 JSON 응답을 반환
        async function requestJson(url, options) {

            const response = await fetch(
                    url,
                    options || {}
            );

            if (!response.ok) {
                throw new Error(
                        "요청 처리에 실패했습니다. 상태 코드: "
                        + response.status
                );
            }

            return response.json();
        }

        // 알림 목록을 드롭다운 내부에 출력
        function renderNotifications() {

            notificationList.replaceChildren();

            if (notifications.length === 0) {
                notificationList.appendChild(
                        createEmptyNotificationElement()
                );

                return;
            }

            notifications.forEach(function (notification) {
                notificationList.appendChild(
                        createNotificationElement(notification)
                );
            });
        }

        // 알림 한 건에 해당하는 HTML 요소 생성
        function createNotificationElement(notification) {

            const item = document.createElement("button");
            item.type = "button";
            item.className = "notification-item";

            if (!notification.read) {
                item.classList.add("is-unread");
            }

            const icon = document.createElement("span");
            icon.className = "notification-item-icon";

            const iconImage = document.createElement("i");
            iconImage.className = getNotificationIcon(
                    notification.notificationType
            );

            icon.appendChild(iconImage);

            const content = document.createElement("span");
            content.className = "notification-item-content";

            const message = document.createElement("span");
            message.className = "notification-item-message";
            message.textContent = notification.message;

            const createdAt = document.createElement("span");
            createdAt.className = "notification-item-time";
            createdAt.textContent = formatCreatedAt(
                    notification.createdAt
            );

            content.appendChild(message);
            content.appendChild(createdAt);

            item.appendChild(icon);
            item.appendChild(content);

            if (!notification.read) {
                const unreadDot = document.createElement("span");
                unreadDot.className = "notification-unread-dot";
                unreadDot.setAttribute("aria-label", "읽지 않은 알림");

                item.appendChild(unreadDot);
            }

            item.addEventListener("click", async function () {
                await openNotification(notification);
            });

            return item;
        }

        // 알림을 읽음 처리한 뒤 연결된 화면으로 이동
        async function openNotification(notification) {

            if (!notification.read) {
                try {
                    await requestJson(
                            "/api/notifications/"
                            + notification.notificationId
                            + "/read",
                            {
                                method: "PATCH",
                                headers: createCsrfHeaders()
                            }
                    );

                    notification.read = true;
                    unreadCount = Math.max(0, unreadCount - 1);

                    renderNotifications();
                    updateUnreadCount();

                } catch (error) {
                    showNotificationError(
                            "알림 읽음 처리에 실패했습니다."
                    );

                    return;
                }
            }

            if (notification.linkUrl) {
                window.location.href = notification.linkUrl;
            }
        }

        // 로그인 회원의 모든 알림 읽음 처리
        async function readAllNotifications() {

            if (unreadCount === 0) {
                return;
            }

            readAllButton.disabled = true;

            try {
                await requestJson(
                        "/api/notifications/read-all",
                        {
                            method: "PATCH",
                            headers: createCsrfHeaders()
                        }
                );

                notifications.forEach(function (notification) {
                    notification.read = true;
                });

                unreadCount = 0;

                renderNotifications();
                updateUnreadCount();

            } catch (error) {
                showNotificationError(
                        "전체 알림 읽음 처리에 실패했습니다."
                );

            } finally {
                readAllButton.disabled = unreadCount === 0;
            }
        }

        // 읽지 않은 알림 개수를 배지와 안내 문구에 반영
        function updateUnreadCount() {

            if (unreadCount > 0) {
                notificationBadge.textContent =
                        unreadCount > 99 ? "99+" : unreadCount;

                notificationBadge.classList.remove("d-none");

                notificationSummary.textContent =
                        "읽지 않은 알림 " + unreadCount + "개가 있습니다.";

                readAllButton.disabled = false;

            } else {
                notificationBadge.textContent = "0";
                notificationBadge.classList.add("d-none");

                notificationSummary.textContent =
                        "새로운 알림이 없습니다.";

                readAllButton.disabled = true;
            }
        }

        // 데이터 변경 요청에 Spring Security의 CSRF 토큰 추가
        function createCsrfHeaders() {

            const headers = {};

            if (csrfTokenElement && csrfHeaderElement) {
                headers[csrfHeaderElement.content] =
                        csrfTokenElement.content;
            }

            return headers;
        }

        // WebSocket과 STOMP를 이용해 개인 알림 채널 구독
        function connectWebSocket() {

            if (typeof SockJS === "undefined"
                    || typeof Stomp === "undefined") {

                console.error(
                        "[IssueFlow] SockJS 또는 STOMP 라이브러리를 불러오지 못했습니다."
                );

                return;
            }

            console.info("[IssueFlow] WebSocket 연결을 시도합니다: /ws");

            const socket = new SockJS("/ws");

            stompClient = Stomp.over(socket);
            stompClient.debug = null;

            stompClient.connect(
                    {},
                    function () {
                        clearReconnectTimer();

                        console.info("[IssueFlow] STOMP 연결에 성공했습니다.");

                        stompClient.subscribe(
                                "/user/queue/notifications",
                                function (message) {
                                    console.info(
                                            "[IssueFlow] 실시간 알림을 수신했습니다.",
                                            message.body
                                    );

                                    receiveNotification(message.body);
                                }
                        );

                        console.info(
                                "[IssueFlow] 개인 알림 채널을 구독했습니다: "
                                + "/user/queue/notifications"
                        );
                    },
                    function (error) {
                        console.error(
                                "[IssueFlow] STOMP 연결에 실패했습니다.",
                                error
                        );

                        scheduleReconnect();
                    }
            );
        }

        // WebSocket으로 받은 JSON 알림을 목록 맨 위에 추가
        function receiveNotification(messageBody) {

            let notification;

            try {
                notification = JSON.parse(messageBody);

            } catch (error) {
                console.error(
                        "[IssueFlow] 알림 JSON 변환에 실패했습니다.",
                        error
                );

                return;
            }

            const duplicated = notifications.some(
                    function (savedNotification) {
                        return savedNotification.notificationId
                                === notification.notificationId;
                    }
            );

            if (duplicated) {
                return;
            }

            notifications.unshift(notification);

            if (notifications.length > 20) {
                notifications.pop();
            }

            if (!notification.read) {
                unreadCount += 1;
            }

            renderNotifications();
            updateUnreadCount();
        }

        // WebSocket 연결이 끊어지면 5초 뒤 다시 연결
        function scheduleReconnect() {

            if (reconnectTimer !== null) {
                return;
            }

            reconnectTimer = window.setTimeout(
                    function () {
                        reconnectTimer = null;
                        connectWebSocket();
                    },
                    5000
            );
        }

        function clearReconnectTimer() {

            if (reconnectTimer === null) {
                return;
            }

            window.clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }

        // 알림 종류에 맞는 Bootstrap 아이콘 반환
        function getNotificationIcon(notificationType) {

            const iconByType = {
                PROJECT_MEMBER_ADDED: "bi bi-person-plus",
                PROJECT_MEMBER_REMOVED: "bi bi-person-dash",
                ISSUE_CREATED: "bi bi-plus-square",
                ISSUE_ASSIGNEE_CHANGED: "bi bi-person-check",
                ISSUE_STATUS_CHANGED: "bi bi-arrow-repeat",
                COMMENT_CREATED: "bi bi-chat-left-text",
                ISSUE_DELETED: "bi bi-trash"
            };

            return iconByType[notificationType]
                    || "bi bi-bell";
        }

        // 알림 생성 시각을 한국어 화면 형식으로 변환
        function formatCreatedAt(createdAt) {

            if (!createdAt) {
                return "";
            }

            const createdDate = new Date(createdAt);

            if (Number.isNaN(createdDate.getTime())) {
                return "";
            }

            return new Intl.DateTimeFormat(
                    "ko-KR",
                    {
                        month: "numeric",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit"
                    }
            ).format(createdDate);
        }

        function createEmptyNotificationElement() {

            const empty = document.createElement("div");
            empty.className = "notification-empty";

            const icon = document.createElement("i");
            icon.className = "bi bi-bell-slash";

            const message = document.createElement("p");
            message.textContent = "도착한 알림이 없습니다.";

            empty.appendChild(icon);
            empty.appendChild(message);

            return empty;
        }

        function showNotificationError(errorMessage) {

            notificationList.replaceChildren();

            const error = document.createElement("div");
            error.className = "notification-error";
            error.textContent = errorMessage;

            notificationList.appendChild(error);
        }
    });
}());