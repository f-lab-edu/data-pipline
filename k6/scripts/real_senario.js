import { WebSocket } from 'k6/experimental/websockets';
import { check, sleep } from 'k6';
import http from 'k6/http';
import { Counter } from 'k6/metrics';


const MATCH_API_URL = __ENV.MATCH_API_URL;
const WS_URL = __ENV.WS_URL;
const playerMovedEventsReceived = new Counter('player_moved_events_received');

export const options = {
    scenarios: {
        match_scenario: {
            executor: 'per-vu-iterations',
            vus: 4,
            iterations: 1,
            maxDuration: '1m',
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.1'],
        'http_req_duration': ['p(95)<500'],
        'player_moved_events_received': ['count>0'],

    },
};

export default function () {
    const sessionId = simpleUUID();

    const ws = new WebSocket(`${WS_URL}?sessionId=${sessionId}`);
    let matchRes = http.post(MATCH_API_URL, JSON.stringify({}), {
            headers: {
                'Content-Type': 'application/json',
                'X-Session-Id': sessionId
            }
        });
        console.log(`[VU ${__VU}] matchRes.status=`, matchRes.status);
        console.log(`[VU ${__VU}] matchRes.body=`, matchRes.body);

    let jsonData;
    try {
        jsonData = matchRes.json();
    } catch (error) {
        console.error(`[VU ${__VU}] JSON 파싱 중 오류: ${error}, 응답 본문: ${matchRes.body}`);
    }
    const matchStatus = jsonData.status;
    let matchConfirmed = false;

    check(matchRes, {
        '매칭 요청 성공': (r) => r.status === 200 || r.status === 201,
    }) || (__ENV.abortOnFail && fail('매칭 요청 실패 발생, 테스트 중단'));

    if (matchStatus === "WAITING") {
        console.log(`[VU ${__VU}] 매칭 대기 중입니다...`);
    } else if (matchStatus === "MATCHED") {
        console.log(`[VU ${__VU}] 매칭 완료: ${JSON.stringify(jsonData)}`);
    } else {
        console.warn(`[VU ${__VU}] 예상치 못한 매칭 상태: ${matchStatus}`);
    }


    ws.onopen = () => {
        const checkMatch = setInterval(() => {
            clearInterval(checkMatch);
            setInterval(() => {
                const { x, y } = generatePosition();
                ws.send(JSON.stringify({
                    type: 'move',
                    data: {
                        currentPosition: { x, y },
                        direction: getRandomDirection(),
                        speed: 5,
                    },
                }));
            }, 250);
        }, 500);
    };

    ws.onmessage = (msg) => {
        try {
            const event = JSON.parse(msg.data);
            if (event.eventType === "PLAYER_MOVED") {
                playerMovedEventsReceived.add(1);
                console.log(`[VU ${__VU}] PlayerMoved 이벤트 수신: `, event);
            }
        } catch (error) {
            console.error(`[VU ${__VU}] 메시지 파싱 오류: `, msg.data, error);
        }
    };


    ws.onerror = (e) => console.error(`[VU ${__VU}] Error:`, e.error);

    setTimeout(() => {
        ws.close();
    }, __ENV.TEST_DURATION || 300000);

    sleep(1);
}

// 헬퍼 함수들
function generatePosition() {
    return { x: Math.random() * 800, y: Math.random() * 600 };
}

function getRandomDirection() {
    const dirs = ['UP', 'DOWN', 'LEFT', 'RIGHT'];
    return dirs[Math.floor(Math.random() * dirs.length)];
}

function simpleUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}
