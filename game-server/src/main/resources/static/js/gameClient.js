const socket = window.socket
if (!socket || socket.readyState !== WebSocket.OPEN) {
    throw new Error("게임 진입을 위한 웹소켓 연결이 없습니다. 로비 연결부터 다시 확인해주세요.");
}


    const canvas = document.getElementById("gameCanvas");
const context = canvas.getContext("2d");

let player = { x: 400, y: 300, width: 20, height: 20, color: "blue" };
let enemies = [];
const enemyColors = {
    Zergling: "green",
    Hydralisk: "purple"
};
let round = 1;

// WebSocket 연결 후 이벤트 처리
socket.addEventListener("open", () => {
    console.log("WebSocket 연결 성공!");
});

socket.addEventListener("message", (event) => {
    const data = JSON.parse(event.data);
    handleServerMessage(data);
});

socket.addEventListener("close", () => {
    console.log("WebSocket 연결 종료");
});

socket.addEventListener("error", (error) => {
    console.error("WebSocket 오류:", error);
});

// 서버에서 수신된 메시지 처리 함수
function handleServerMessage(response) {
    if (response.type === "enemy_spawn") {
        enemies = response.data.map((enemy) => {
            const { status } = enemy;
            return {
                enemyId: enemy.enemyId,
                x: enemy.position.x,
                y: enemy.position.y,
                health: status.health,
                maxHealth: status.health,
                damage: status.damage,
                defense: status.defense,
                width: status.width,
                height: status.height,
                color: enemyColors[status.name] || "red"
            };
        });
        round = response.round;
        console.log(`라운드 ${round}: 적 ${enemies.length}명 생성됨.`);
    } else if (response.type === "move") {
        if (response.success) {
            const { x, y } = response.data.newPosition;
            player.x = x;
            player.y = y;
        } else {
            console.error("이동 실패:", response.message);
        }
    } else if (response.type === "enemy_positions") {
        console.log("Received Enemy Positions:", response.data);
        response.data.forEach((updatedEnemy) => {
            const existingEnemy = enemies.find((enemy) => enemy.enemyId === updatedEnemy.enemyId);
            if (existingEnemy) {
                existingEnemy.x = updatedEnemy.position.x;
                existingEnemy.y = updatedEnemy.position.y;
            } else {
                console.warn(`Enemy with ID ${updatedEnemy.enemyId} not found!`);
            }
        });
    }
}

// 플레이어 이동 요청 전송
function sendPlayerMoveRequest(direction) {
    const payload = {
        type: "move",
        data: {
            currentPosition: { x: player.x, y: player.y },
            direction: direction,
            speed: 5
        }
    };

    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(payload));
    }
}

// 키 이벤트 처리
const keys = {};
document.addEventListener("keydown", (event) => {
    keys[event.key] = true;
});
document.addEventListener("keyup", (event) => {
    keys[event.key] = false;
});

// 현재 키 상태에 따른 방향 계산 함수
function getDirection() {
    if (keys["ArrowUp"]) return "UP";
    if (keys["ArrowDown"]) return "DOWN";
    if (keys["ArrowLeft"]) return "LEFT";
    if (keys["ArrowRight"]) return "RIGHT";
    return null;
}

// 게임 루프 함수
function gameLoop() {
    const direction = getDirection();
    if (direction) {
        sendPlayerMoveRequest(direction);
    }
    renderScene();
    requestAnimationFrame(gameLoop);
}

// 게임 화면 렌더링 함수
function renderScene() {

    context.clearRect(0, 0, canvas.width, canvas.height);

    context.fillStyle = player.color;
    context.fillRect(player.x, player.y, player.width, player.height);

    enemies.forEach((enemy) => {
        context.fillStyle = enemy.color;
        context.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);

        context.fillStyle = "red";
        const healthBarWidth = (enemy.health / enemy.maxHealth) * enemy.width;
        context.fillRect(enemy.x, enemy.y - 5, healthBarWidth, 3);
    });

    context.fillStyle = "white";
    context.font = "20px Arial";
    context.fillText(`Round: ${round}`, 10, 30);
}

// 게임 시작
gameLoop();
