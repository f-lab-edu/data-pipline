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

function requestMultiMatch() {
    // localStorage에 저장된 sessionId를 사용합니다.
    const sessionId = localStorage.getItem("sessionId");

    if (!sessionId) {
        alert("세션 정보가 없습니다. 다시 로그인 해주세요.");
        window.location.href = '/';
        return;
    }

    fetch("/api/v1/match/multi", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Session-Id": sessionId
        }
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("멀티 매칭 요청 실패");
            }
            return response.json();
        })
        .then((matchResult) => {
            console.log("매칭 결과:", matchResult);
            // 매칭 상태에 따라 처리합니다.
            if (matchResult.status === "MATCHED") {
                alert("매칭 성공! 게임을 시작합니다.");
                // 게임 시작에 필요한 WebSocket URL 등을 localStorage에 저장
                if (matchResult.websocketUrl) {
                    localStorage.setItem("websocketUrl", matchResult.websocketUrl);
                }
                // 매칭이 성사되면 게임 페이지로 이동합니다.
                window.location.href = "/ws/game";
            } else if (matchResult.status === "WAITING") {
                // 매칭이 아직 대기 중일 때 UI 업데이트
                const multiMatchBtn = document.getElementById("multiMatchBtn");
                // 버튼을 비활성화하여 중복 요청을 방지합니다.
                multiMatchBtn.disabled = true;

                let statusMessage = document.getElementById("statusMessage");
                if (!statusMessage) {
                    // statusMessage 영역이 없으면 새로운 span 요소를 추가
                    statusMessage = document.createElement("span");
                    statusMessage.id = "statusMessage";
                    // 버튼 아래나 원하는 위치에 부착할 수 있습니다.
                    multiMatchBtn.parentNode.appendChild(statusMessage);
                }
                statusMessage.innerText = "매칭 대기 중입니다...";
                // 추가로, UI에 대기 상황을 나타내는 스피너나 애니메이션을 넣을 수도 있습니다.
            }
        })
        .catch((error) => {
            console.error("매칭 요청 처리 중 오류 발생:", error);
            alert("매칭 과정에서 오류가 발생했습니다. 다시 시도해주세요.");
        });
}
