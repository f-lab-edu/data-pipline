// URL에서 sessionId를 추출하는 함수
function getSessionIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get("sessionId");
}

document.addEventListener("DOMContentLoaded", () => {
    // URL에 sessionId가 존재하면 localStorage에 저장합니다.
    const sessionId = getSessionIdFromUrl();
    if (sessionId) {
        localStorage.setItem("sessionId", sessionId);
    }

    const multiMatchBtn = document.getElementById("multiMatchBtn");
    multiMatchBtn.addEventListener("click", () => {
        requestMultiMatch();
    });
});

// 웹소켓 연결 및 이벤트 처리 함수
function connectWebSocket(sessionKey) {
    if (window.socket && window.socket.readyState === WebSocket.OPEN) {
        console.log("이미 웹소켓 연결이 존재합니다.");
        return;
    }

    window.socket = new WebSocket("ws://localhost:8080/ws/game?sessionId=" + sessionKey);

    // 메시지 수신 시 이벤트 처리
    window.socket.addEventListener("message", function(event) {
        const data = JSON.parse(event.data);

        // "MATCHED" 상태를 웹소켓으로부터 전달받으면 화면을 전환합니다.
        if (data.status === "MATCHED") {
            alert("매칭 성공! 게임을 시작합니다.");

            document.getElementById("lobby").style.display = "none";
            document.getElementById("gameContainer").style.display = "block";

            // 게임 클라이언트 스크립트 동적 로딩
            const gameScript = document.createElement("script");
            gameScript.src = "js/gameClient.js";
            gameScript.onload = function() {
                console.log("게임 클라이언트가 로드되었습니다.");
            };
            document.body.appendChild(gameScript);
        }

    });

    window.socket.addEventListener("open", () => {
        console.log("웹소켓 연결 성공");
    });

    window.socket.addEventListener("close", () => {
        console.log("웹소켓 연결 종료");
    });

    window.socket.addEventListener("error", (error) => {
        console.error("웹소켓 연결 에러:", error);
    });
}

function requestMultiMatch() {
    const sessionId = localStorage.getItem("sessionId");

    if (!sessionId) {
        alert("세션 정보가 없습니다. 다시 로그인 해주세요.");
        window.location.href = '/login';
        return;
    }

    // 멀티 매칭 요청을 보낼 때 웹소켓 연결 미리 설정
    connectWebSocket(sessionId);

    fetch("/api/v1/match/multi", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Session-Id": sessionId
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("매칭 요청에 실패했습니다.");
            }
            return response.json();
        })
        .then(matchResult => {
            console.log("매칭 요청 결과(JSON):", matchResult);

            if (matchResult.status === "WAITING") {
                const multiMatchBtn = document.getElementById("multiMatchBtn");
                multiMatchBtn.disabled = true;
                let statusMessage = document.getElementById("statusMessage");
                if (!statusMessage) {
                    statusMessage = document.createElement("span");
                    statusMessage.id = "statusMessage";
                    multiMatchBtn.parentNode.appendChild(statusMessage);
                }
                statusMessage.innerText = "매칭 대기 중입니다...";
            }
            // 여기서는 "MATCHED" 상태를 기다리지 않습니다.
            // 서버가 웹소켓을 통해 별도로 매칭 완료를 알려줄 것입니다.
        })
        .catch(error => {
            console.error("매칭 요청 처리 중 오류 발생:", error);
            alert("매칭 과정에서 오류가 발생했습니다. 다시 시도해주세요.");
        });
}