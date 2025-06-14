3조 벌고 싶다
---

[3조 벌고 싶다](https://www.notion.so/207649136c11806aaca3cedbb88a8b05?pvs=21) - Notion Link

## 팀원 구성

강우진 ( https://github.com/WJKANGsw )

김현기 (https://github.com/LZHTK ) 

이채원 ( https://github.com/Chaewon3Lee ) 

임정현 ( https://github.com/HuInDoL ) 

황지인 ( https://github.com/wangcoJiin )

---

## Findex

![전체샷](https://github.com/user-attachments/assets/bc2dec93-05f1-4ca9-815f-c074dbe23c0e)


- 가볍고 빠른 외부 API 연동 금융 분석 도구
- 금융 지수 데이터를 한눈에 제공하는 대시보드 서비스
- 프로젝트 기간 : 06.03 ~ 06.13

---

## 기술 스택

| **분류** | **사용 예정 도구** |
| --- | --- |
| Backend | Spring Boot(3.5.0), Spring Data JPA, openJDK(17.0.14), IntelliJ(2025.1) |
| Database | PostgreSQL(17.5) |
| API 문서화 | Swagger , springdoc-openapi(v2.8.4) |
| 협업 도구 | Discord, GitHub, Notion |
| 일정 관리 | GitHub Issues + Notion 타임라인 |

---

## 팀원별 구현 기능 상세

## 강우진

![image](https://github.com/user-attachments/assets/70dcfc78-7ffa-4adc-9f3a-c0358cf78d5c)

- 지수 데이터 등록
    - **{지수}**, **{날짜}**부터 **{상장 시가 총액}**까지 모든 속성을 입력해 지수 데이터를 등록할 수 있습니다.
        - **{지수}, {날짜}** 조합값은 중복되면 안됩니다.
    - Open API를 활용해 자동으로 등록할 수 있습니다.
- 지수 데이터 수정
    - **{지수}, {날짜}**를 제외한 모든 속성을 수정할 수 있습니다.
    - Open API를 활용해 자동으로 수정할 수 있습니다.
- 지수 데이터 삭제
    - 지수 데이터를 삭제할 수 있습니다.
- 지수 데이터 연동
    - pen API를 활용해 지수 데이터를 등록, 수정할 수 있습니다.
    - **{지수}, {대상 날짜}**로 연동할 데이터의 범위를 지정할 수 있습니다.
        - **{대상 날짜}**는 반드시 지정해야하며, 범위로 지정할 수 있습니다.
        - **{지수}**는 선택적으로 지정할 수 있습니다.
    - 지수 데이터 연동은 사용자가 직접 실행할 수 있습니다.
        - 실행 후 연동 작업 결과를 등록합니다.
        - 대상 지수, 대상 날짜가 여러 개인 경우 지수, 날짜 별로 이력을 등록합니다.
- 배치에 의한 지수 데이터 연동 자동화
    - **지수 데이터 연동 프로세스**를 일정한 주기(1일)마다 자동으로 반복합니다.
        - 배치 주기는 애플리케이션 설정을 통해 주입할 수 있어야 합니다.
        - `Spring Scheduler`를 활용해 구현하세요.
    - 연동할 지수는 지수 연동 설정이 활성화되어 있는 지수입니다.
    - **{대상 날짜}**는 해당 지수의 마지막 자동 연동 작업 날짜부터 가장 최신 날짜까지입니다.
---

## 김현기

![대시보드](https://github.com/user-attachments/assets/bd56b499-c22b-4f08-a55f-ecce17a4c552)


- 지수 정보 등록
    - **지수 정보 등록**
    - **{지수 분류명}**, **{지수명}**, **{채용 종목 수}**, **{기준 시점}**, **{기준 지수}**, **{즐겨찾기}**를 통해 지수 정보를 등록할 수 있습니다.
        - **{지수 분류명}**, **{지수명}** 조합값은 중복되면 안됩니다.
    - Open API를 활용해 자동으로 등록할 수 있습니다.
    - **자동 연동 설정 정보**도 같이 초기화되어야 합니다.
- 지수 정보 수정
    - **{채용 종목 수}, {기준 시점}, {기준 지수}, {즐겨찾기}**을 수정할 수 있습니다.
    - **{채용 종목 수}, {기준 시점}, {기준 지수}**는 Open API를 활용해 자동으로 수정할 수 있습니다.
- 지수 정보 삭제
    - 지수 정보를 삭제하면 관련된 지수 데이터도 같이 삭제되어야 합니다.
- 주요 지수 현황 요약
    - **{즐겨찾기}**된 지수의 성과 정보를 포함합니다.
        - 성과는 **{종가}**를 기준으로 비교합니다.
- 지수 차트
    - 월/분기/년 단위 시계열 데이터
        - **{종가}**를 기준으로 합니다.
    - 이동평균선 데이터
        - 5일, 20일
        - 이동평균선이란 지난 n일간 가격의 평균을 의미합니다.
        - 예를 들어 3월 13일의 5일 이동평균선은 3월 9일부터 3월 13일까지의 **{종가}**의 평균값입니다.
- 지수 성과 분석 랭킹
    - 전일/전주/전월 대비 성과 랭킹
        - 성과는 **{종가}**를 기준으로 비교합니다.

---

## 이채원

![image](https://github.com/user-attachments/assets/11a0407e-6648-411d-88fe-97fdab96e552)

- 지수 데이터 목록 조회
    - **{지수}, {날짜}**로 지수 데이터 목록을 조회할 수 있습니다.
        - **{지수}**는 완전 일치 조건입니다.
        - **{날짜}**는 범위 조건입니다.
        - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회합니다.
    - **{소스 타입}**을 제외한 모든 속성으로 정렬 및 페이지네이션을 구현합니다.
        - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가질 수 있습니다.
        - 정확한 페이지네이션을 위해 **{이전 페이지의 마지막 요소 ID}**를 활용합니다.
        - 화면을 고려해 적절한 페이지네이션 전략을 선택합니다.
- 지수 데이터 Export
    - 지수 데이터를 CSV 파일로 Export 할 수 있습니다.
    - Export할 지수 데이터를 지수 데이터 목록 조회와 같은 규칙으로 필터링 및 정렬할 수 있습니다.
        - 페이지네이션은 고려하지 않습니다.
- 자동 연동 설정 목록 조회
    - **{지수}, {활성화}**로 자동 연동 설정 목록을 조회할 수 있습니다.
        - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회합니다.
    - **{지수}, {활성화}**로 정렬 및 페이지네이션을 구현합니다.
        - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가질 수 있습니다.
        - 정확한 페이지네이션을 위해 **{이전 페이지의 마지막 요소 ID}**를 활용합니다.
        - 화면을 고려해 적절한 페이지네이션 전략을 선택합니다.
    - 목록 조회 시 다음의 정보를 포함합니다.
        - **{지수}**
        - **{활성화}**
- 자동 연동 설정 등록
    - 모든 속성을 통해 자동 연동 설정을 등록할 수 있습니다.
    - 지수가 등록될 때 비활성화 상태로 같이 등록됩니다.
- 지수 데이터 연동
    - pen API를 활용해 지수 데이터를 등록, 수정할 수 있습니다.
    - **{지수}, {대상 날짜}**로 연동할 데이터의 범위를 지정할 수 있습니다.
        - **{대상 날짜}**는 반드시 지정해야하며, 범위로 지정할 수 있습니다.
        - **{지수}**는 선택적으로 지정할 수 있습니다.
    - 지수 데이터 연동은 사용자가 직접 실행할 수 있습니다.
        - 실행 후 연동 작업 결과를 등록합니다.
        - 대상 지수, 대상 날짜가 여러 개인 경우 지수, 날짜 별로 이력을 등록합니다.

---

## 임정현

![image](https://github.com/user-attachments/assets/4b4663f2-69ea-4a24-a932-87c248b2fd6e)

- 연동 정보
    - 연동 작업은 다음의 정보를 가집니다.
        - **{유형}**: `지수 정보`, `지수 데이터`
        - **{지수}**: 연동된 지수 정보입니다.
        - **{대상 날짜}**: 연동된 데이터의 **{날짜}**입니다.
        - **{작업자}**
            - 사용자가 직접 연동한 경우 요청 IP
            - 배치에 의해 연동된 경우 `system`
        - **{작업일시}**
        - **{결과}**: `성공`, `실패`
- 지수 정보 연동
    - Open API를 활용해 지수 정보를 등록, 수정할 수 있습니다.
    - 지수 정보 연동은 사용자가 직접 실행할 수 있습니다.
        - 실행 후 연동 작업 결과를 등록합니다.
        - 대상 지수가 여러 개인 경우 지수 별로 이력을 등록합니다.
- 자동 연동 설정 Entity, Dto
    - **{지수}**: 해당 지수 ID
    - **{활성화}**
- 자동 연동 설정 등록
    - 모든 속성을 통해 자동 연동 설정을 등록할 수 있습니다.
    - 지수가 등록될 때 비활성화 상태로 같이 등록됩니다.
- 자동 연동 설정 수정
    - **{활성화}** 속성을 수정할 수 있습니다.
- 자동 연동 설정 목록 조회
    - **{지수}, {활성화}**로 자동 연동 설정 목록을 조회할 수 있습니다.
        - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회합니다.
    - **{지수}, {활성화}**로 정렬 및 페이지네이션을 구현합니다.
        - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가질 수 있습니다.
        - 정확한 페이지네이션을 위해 **{이전 페이지의 마지막 요소 ID}**를 활용합니다.
        - 화면을 고려해 적절한 페이지네이션 전략을 선택합니다.
    - 목록 조회 시 다음의 정보를 포함합니다.
        - **{지수}**
        - **{활성화}**

---

## 황지인

**지수 정보 연동**
![녹화_2025_06_13_17_45_13_226](https://github.com/user-attachments/assets/d860dde5-0719-44a8-8bf2-a936b508f7fb)

- 금융 시세 정보 Open API를 활용해 지수 정보를 등록하는 기능 (WebClient 사용)
  - 지수 정보 연동은 사용자가 직접 실행
  - 실행 후 연동 작업 결과를 데이터베이스에 등록
  - 대상 지수가 여러 개인 경우 지수 별로 이력을 등록

**지수 정보 조회**
![녹화_2025_06_13_17_46_21_940](https://github.com/user-attachments/assets/64bb96c0-0897-46bc-8e3c-0598fafa371d)

- {지수 분류명}, {지수명}, {즐겨찾기}로 지수 정보 목록을 조회하는 기능
  - {지수 분류명}, {지수명}은 부분 일치 조건
  - {즐겨찾기}는 완전 일치 조건
  - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회
- {지수 분류명}, {지수명}으로 정렬 및 페이지네이션을 구현(JPA Specification 사용)
  - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가짐
  - 정확한 페이지네이션을 위해 {이전 페이지의 마지막 요소 ID}를 활용

**연동 작업 목록 조회**
![녹화_2025_06_13_17_48_17_851](https://github.com/user-attachments/assets/d7fd8c37-6b06-4008-a241-c0ae767d22dc)

- {유형}, {지수}, {대상 날짜}, {작업자}, {결과}, {작업일시}로 연동 작업 목록을 조회하는 기능
  - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회
- {대상 날짜}, {작업일시}으로 정렬 및 페이지네이션을 구현 (JPA Specification 사용)
  - 여러 개의 정렬 조건 중 선택적으로 1개의 정렬 조건만 가짐
  - 정확한 페이지네이션을 위해 {이전 페이지의 마지막 요소 ID}를 활용

---

## 파일구조

```java
src
├── main
│   ├── java
│   │   └── com
│   │       └── sprint
│   │           └── findex
│   │               ├── apidocs
│   │               │   └── api-docs.json
│   │               ├── controller
│   │               │   ├── AutoSyncConfigController
│   │               │   ├── IndexDataController
│   │               │   ├── IndexInfoController
│   │               │   ├── SyncJobController
│   │               │   └── api
│   │               │       ├── AutoSyncConfigApi
│   │               │       ├── IndexDataApi
│   │               │       ├── IndexInfoApi
│   │               │       └── SyncJobApi
│   │               ├── dto
│   │               │   ├── dashboard
│   │               │   │   ├── ChartPoint
│   │               │   │   ├── IndexChartDto
│   │               │   │   ├── IndexPerformanceDto
│   │               │   │   └── RankedIndexPerformanceDto
│   │               │   ├── request
│   │               │   │   ├── AutoSyncConfigUpdateRequest
│   │               │   │   ├── AutoSyncQueryParams
│   │               │   │   ├── IndexDataCreateRequest
│   │               │   │   ├── IndexDataQueryParams
│   │               │   │   ├── IndexDataSyncRequest
│   │               │   │   ├── IndexDataUpdateRequest
│   │               │   │   ├── IndexInfoCreateCommand
│   │               │   │   ├── IndexInfoCreateRequest
│   │               │   │   ├── IndexInfoUpdateRequest
│   │               │   │   └── SyncJobQueryParams
│   │               │   └── response
│   │               │       ├── cursor
│   │               │       │   ├── CursorPageResponseAutoSyncConfigDto
│   │               │       │   ├── CursorPageResponseIndexData
│   │               │       │   ├── CursorPageResponseIndexInfoDto
│   │               │       │   └── CursorPageResponseSyncJobDto
│   │               │       ├── AutoSyncConfigDto
│   │               │       ├── ErrorResponse
│   │               │       ├── IndexDataCsvExporter
│   │               │       ├── IndexDataDto
│   │               │       ├── IndexDataSyncResponse
│   │               │       ├── IndexInfoDto
│   │               │       ├── IndexInfoSearchDto
│   │               │       ├── IndexInfoSummaryDto
│   │               │       ├── ResponseCursorDto
│   │               │       ├── ResponseSyncJobCursorDto
│   │               │       └── SyncJobDto
│   │               ├── entity
│   │               │   ├── base
│   │               │   │   ├── AutoSyncConfig
│   │               │   │   ├── IndexData
│   │               │   │   ├── IndexInfo
│   │               │   │   └── BaseEntity
│   │               │   ├── Period
│   │               │   ├── SourceType
│   │               │   ├── SyncJob
│   │               │   ├── SyncJobResult
│   │               │   └── SyncJobType
│   │               ├── global
│   │               │   ├── config
│   │               │   ├── dto
│   │               │   ├── exception
│   │               │   └── util
│   │               ├── mapper
│   │               ├── repository
│   │               ├── service
│   │               │   └── basic
│   │               ├── specification
│   │               └── Sb03FindexTeam3Application
│   └── resources
│       ├── static
│       │   ├── assets
│       │   │   ├── index-CGZC7fCi.js
│       │   │   ├── index-DKaYgyvc.js
│       │   │   └── index-Dtn62Xmo.css
│       │   ├── favicon.ico
│       │   └── index.html
│       ├── application.yaml
│       ├── dashboard.http
│       ├── index-info.http
│       └── schema.sql
└── test
    └── java
        └── com
            └── sprint
                └── findex
                    └── Sb03FindexTeam3ApplicationTests
```

---

## 구현 홈페이지

https://sb03-findex-team3-production.up.railway.app/#/dashboard

---

## 프로젝트 회고록

https://www.notion.so/ohgiraffers/3-4L-211649136c118021a909c72c8875362f

---
