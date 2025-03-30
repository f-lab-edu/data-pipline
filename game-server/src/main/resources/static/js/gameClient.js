const socket = window.socket;
if (!socket || socket.readyState !== WebSocket.OPEN) {
    throw new Error("게임 진입을 위한 웹소켓 연결이 없습니다. 로비 연결부터 다시 확인해주세요.");
}

const canvas = document.getElementById("gameCanvas");
const context = canvas.getContext("2d");

let otherPlayers = {}; // 다른 플레이어 객체 목록
const OTHER_PLAYER_COLOR = "orange";

let player = {
    x: 400, y: 300, width: 20, height: 20, color: "blue", speed: 200
};
let lastSentTime = Date.now();

let enemies = [];
const enemyColors = {Zergling: "green", Hydralisk: "purple"};
let round = 1;

socket.addEventListener("message", (event) => {
    const data = JSON.parse(event.data);
    handleServerMessage(data);
});

socket.addEventListener("open", () => console.log("WebSocket 연결 성공!"));
socket.addEventListener("close", () => console.log("WebSocket 연결 종료"));
socket.addEventListener("error", (error) => console.error("WebSocket 오류:", error));

function handleServerMessage(response) {
    if (response.eventType === "PLAYER_MOVED") {
        const {playerId, newPositionX, newPositionY} = response;
        if (!otherPlayers[playerId]) {
            const assignedColor = window.playerColors[playerId] || OTHER_PLAYER_COLOR;
            otherPlayers[playerId] = {
                currentX: newPositionX,
                currentY: newPositionY,
                targetX: newPositionX,
                targetY: newPositionY,
                width: 20,
                height: 20,
                color: assignedColor
            };
        } else {
            otherPlayers[playerId].targetX = newPositionX;
            otherPlayers[playerId].targetY = newPositionY;
        }
    }


    else if (response.type === "enemy_spawn") {
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

// 키 상태 처리
const keys = {};
document.addEventListener("keydown", (event) => keys[event.key] = true);
document.addEventListener("keyup", (event) => keys[event.key] = false);

// 방향 계산
function getDirection() {
    if (keys["ArrowUp"]) return "UP";
    if (keys["ArrowDown"]) return "DOWN";
    if (keys["ArrowLeft"]) return "LEFT";
    if (keys["ArrowRight"]) return "RIGHT";
    return null;
}

// 보간함수
function interpolate(current, target, deltaTime, speed = 10) {
    return current + (target - current) * deltaTime * speed;
}

function sendPlayerMoveRequest(direction) {
    const payload = {
        type: "move", data: {
            currentPosition: {x: player.x, y: player.y}, direction: direction, speed: 5
        }
    };
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(payload));
    }
}

// 즉시 클라이언트 예측 이동
function movePlayerPredictively(direction, deltaTime) {
    switch (direction) {
        case "UP":
            player.y -= player.speed * deltaTime;
            break;
        case "DOWN":
            player.y += player.speed * deltaTime;
            break;
        case "LEFT":
            player.x -= player.speed * deltaTime;
            break;
        case "RIGHT":
            player.x += player.speed * deltaTime;
            break;
    }

    if ((Date.now() - lastSentTime) > 50) {
        sendPlayerMoveRequest(direction);
        lastSentTime = Date.now();
    }
}

// 게임 렌더링 루프
let lastRender = Date.now();

function gameLoop() {
    const now = Date.now();
    const deltaTime = (now - lastRender) / 1000;
    lastRender = now;

    const direction = getDirection();
    if (direction) {
        movePlayerPredictively(direction, deltaTime);
    }

    for (const playerId in otherPlayers) {
        const p = otherPlayers[playerId];
        p.currentX = interpolate(p.currentX, p.targetX, deltaTime);
        p.currentY = interpolate(p.currentY, p.targetY, deltaTime);
    }

    render();
    requestAnimationFrame(gameLoop);
}

function render() {
    context.clearRect(0, 0, canvas.width, canvas.height);

    // 내 플레이어 그리기
    context.fillStyle = player.color;
    context.fillRect(player.x, player.y, player.width, player.height);

    // 다른 플레이어 그리기
    for (const playerId in otherPlayers) {
        const p = otherPlayers[playerId];
        context.fillStyle = p.color;
        context.fillRect(p.currentX, p.currentY, p.width, p.height);
    }

    // 적 그리기
    enemies.forEach(enemy => {
        context.fillStyle = enemy.color;
        context.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
    });
}

// 게임 루프 시작
requestAnimationFrame(gameLoop);