# 👋 Jelly Clash
뱀파이어 서바이벌 류 게임 개발 프로젝트로, 차별화된 4인 협동 디펜스 매칭 시스템 도입을 목표로 하고 있습니다.    
초기 설계에서는 여러 서버 인스턴스를 채널로 사용해, 분리하는 방안을 고려했으나,    
채널마다 유저 수 편차로 인해 매칭이 원활하지 않고 서버 인스턴스 확장이 제약되는 한계가 있을거라 생각했습니다.    
이를 극복하기 위해 분산 컴퓨팅 기반 아키텍처로 전환하여, 채널 개념 없이 물리적으로    
분산된 서버 환경에서도 유저들이 자유롭게 매칭되고 원활한 게임 플레이가 가능하도록 재설계하였습니다.


# 📗 Documentation
[Wiki](https://github.com/f-lab-edu/data-pipline/wiki)

# Play
https://github.com/user-attachments/assets/791b4af4-bdf4-4631-8dbc-754004d2a9db

# ⚙️ System Architecture

<img src="https://github.com/user-attachments/assets/322792d5-4d92-426f-ae09-7ca0a659c969"  width="900" height="650"/>

# 🗂️ 디렉터리 구조
```
data-pipeline/
├─ settings.gradle                   // 멀티 모듈 설정 (core, game-server, infra 포함)
├─ build.gradle                      // 루트 공통 빌드 설정
├─ core/                             // 공통 도메인, DTO, 유틸리티 모듈
│   ├─ build.gradle                  // core 모듈 의존성 및 설정
│   └─ com/game/             
│       ├─ domain/                  // 도메인 클래스, 비즈니스 엔티티 정의
│       ├─ dto/                     // 데이터 전달 객체 (DTO, VO)
│       └─ util/                    // 공통 유틸리티 클래스
│   
├─ game-server/                      // 게임 서비스 모듈 (비즈니스 로직 중심)
│   ├─ build.gradle                  
│   └─ game/server/             
│       ├─ lobby/                   
│       │   ├─ config/              // 로비 설정 (core, infra 모듈 활용)
│       │   ├─ controller/          // REST/WebFlux API (예: 로그인, 매치 신청 등)
│       │   └─ service/             // 로비 비즈니스 로직
│       └─ game/                    
│           ├─ config/              // 게임 관련 설정
│           ├─ controller/          // 게임 진행 관련 API
│           └─ service/             // 게임 비즈니스 로직
│   
└─ infra/                            // 인프라 전용 모듈 (Kafka, Redis, MySQL 연동)
    ├─ build.gradle                  
    └─ game.infra            
        ├─ kafka/                   
        │   ├─ config/              // Kafka 설정 (프로듀서, 컨슈머 등)
        │   ├─ producer/            // 메시지 발행 로직
        │   └─ consumer/            // 메시지 처리 로직
        ├─ redis/                   
        │   └─ config/              // Redis 연결 및 세션 관리 설정
        └─   // MySQL Repository, DataSource 설정 등

```
