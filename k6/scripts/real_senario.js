import {WebSocket} from 'k6/experimental/websockets';
import {check, sleep} from 'k6';
import http from 'k6/http';
import {Counter, Trend} from 'k6/metrics';


const MATCH_API_URL = __ENV.MATCH_API_URL;
const WS_URL = __ENV.WS_URL;
const playerMovedEventsReceived = new Counter('player_moved_events_received');

const wsConnectDuration = new Trend('ws_connect_duration');
const wsMessagesReceived = new Counter('ws_messages_received');
const wsErrors = new Counter('ws_errors');
const wsMessageParseErrors = new Counter('ws_message_parse_errors');
const wsMessageLatency = new Trend('ws_message_latency');
const wsMessageSequenceErrors = new Counter('ws_message_sequence_errors');


export const options = {
    scenarios: {
        websocket_200_users_test: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                {target: 50, duration: '1m'},
                {target: 100, duration: '1m'},
                {target: 150, duration: '1m'},
                {target: 200, duration: '2m'},
                {target: 200, duration: '3m'},
                {target: 0, duration: '1m'},
            ],
            gracefulRampDown: '30s',
        },
    },

    thresholds: {
        'http_req_failed': ['rate<0.1'],
        'http_req_duration': [
            'p(50)<300',
            'p(95)<500',
            'p(99)<1000',
        ],
        'http_req_waiting': ['avg<200'],
        'http_req_connecting': ['avg<100'],
        'http_req_tls_handshaking': ['avg<100'],

        'player_moved_events_received': ['count>0'],

        'ws_connect_duration': ['avg<1500', 'p(95)<2000', 'p(99)<2500', 'max<3000'],
        'ws_messages_received': ['count>0'],
        'ws_errors': ['count==0'],
        'ws_message_parse_errors': ['count==0'],
        'ws_message_latency': [
            'p(50)<100',
            'p(95)<200',
        ],
    },
};


const WS_STATE = {
    CONNECTING: 0,
    OPEN: 1,
    CLOSING: 2,
    CLOSED: 3,
};


export default function () {
    const sessionId = simpleUUID();
    let vuIp = randomIp();
    let sequenceNumber = 0;
    let lastReceivedSequenceNumbers = {};

    const wsStartTime = Date.now();
    const ws = new WebSocket(`${WS_URL}?sessionId=${sessionId}`, null, {
        headers: {
            'X-Forwarded-For': vuIp,
        }
    });

    let matchRes = http.post(MATCH_API_URL, JSON.stringify({}), {
        headers: {
            'Content-Type': 'application/json',
            'X-Session-Id': sessionId,
            'X-Forwarded-For': vuIp,
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

    let interval;
    ws.onopen = () => {
        const connectDuration = Date.now() - wsStartTime;
        wsConnectDuration.add(connectDuration);
        console.log(`[VU ${__VU}] WebSocket 연결 성공, 연결 시간: ${connectDuration}ms`);

        const fpsInterval = 1000 / 20;
        interval = setInterval(() => {
            if (ws.readyState !== WS_STATE.OPEN) {
                console.warn(`[VU ${__VU}] 🚨 WebSocket이 열리지 않음(${ws.readyState}), interval 정지`);
                clearInterval(interval);
                return;
            }
            sequenceNumber++;
            const {x, y} = generatePosition();
            const moveMessage = JSON.stringify({
                type: "move",
                data: {
                    seq: sequenceNumber,
                    senderSessionId: sessionId,
                    timestamp: Date.now(),
                    currentPosition: {x, y},
                    direction: getRandomDirection(),
                    speed: 5,
                },
            });
            ws.send(moveMessage);
        }, fpsInterval);
    };

    ws.onmessage = (msg) => {
        try {
            const event = JSON.parse(msg.data);
            if (event.eventType === "PLAYER_MOVED") {
                wsMessagesReceived.add(1);

                const receiveTime = Date.now();
                playerMovedEventsReceived.add(1);
                console.log(`[VU ${__VU}] PlayerMoved 이벤트 수신: `, event);

                const senderSessionId = event.playerId;
                let expectedSeq = (lastReceivedSequenceNumbers[senderSessionId] || 0) + 1
                if(event.seq !== expectedSeq){
                    wsMessageSequenceErrors.add(1);
                    console.warn(`[VU ${__VU}] 순서 오류! 유저:${senderSessionId} (예상:${expectedSeq}, 수신:${event.seq})`);
                }else{
                    const latency = receiveTime - event.timestamp;
                    wsMessageLatency.add(latency);

                    console.log(`[VU ${__VU}] 유저(${senderSessionId}) 메시지 순서:${event.seq}, latency:${latency}ms`);
                }
                lastReceivedSequenceNumbers[senderSessionId] = event.seq;
            }
        } catch (error) {
            wsMessageParseErrors.add(1);
            console.error(`[VU ${__VU}] 메시지 파싱 오류: `, msg.data, error);
        }
    };

    ws.onerror = (e) => {
        wsErrors.add(1);
        console.error(`[VU ${__VU}] WebSocket 에러 발생`, e);
    };

    ws.onclose = () => {
        console.log(`[VU ${__VU}] WebSocket 연결 종료`);
        clearInterval(interval);
    };
}

// 헬퍼 함수들
function generatePosition() {
    return {x: Math.random() * 800, y: Math.random() * 600};
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

function randomIp() {
    return `${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}.${Math.floor(Math.random() * 256)}`;
}

