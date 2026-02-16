# Spring AI LangGraph-Style Demo

LangGraph의 핵심 패턴(StateGraph, Node, Edge, State)을 **순수 Spring AI만으로** 구현한 데모 프로젝트입니다.

외부 그래프 라이브러리 없이 Spring AI의 ChatClient를 활용하여 LangGraph와 유사한 API를 가진 미니 프레임워크를 만들고, "콘텐츠 생성 파이프라인" 시나리오를 시연합니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.0
- Spring AI 1.0.0 (OpenAI GPT-4o)
- SpringDoc OpenAPI (Swagger UI)
- Maven

## 프로젝트 구조

```
src/main/java/com/example/langgraph/
├── LangGraphDemoApplication.java
├── config/
│   └── OpenAiConfig.java              # ChatClient 빈 설정
├── graph/                              # 그래프 미니 프레임워크
│   ├── GraphState.java                 # 상태 컨테이너 (ConcurrentHashMap)
│   ├── GraphNode.java                  # 노드 인터페이스 (@FunctionalInterface)
│   ├── Edge.java                       # 엣지 record (from → to)
│   ├── NodeEntry.java                  # 내부 record (name + node)
│   ├── StateGraph.java                 # 컴파일된 실행 가능 그래프
│   └── StateGraphBuilder.java          # 플루언트 빌더
├── nodes/                              # 파이프라인 노드 구현체
│   ├── TopicAnalysisNode.java          # 주제 분석
│   ├── ResearchNode.java               # 리서치 포인트 생성
│   ├── DraftWritingNode.java           # 블로그 초안 작성
│   ├── ReviewNode.java                 # 리뷰 & 폴리싱 (LLM 2회 호출)
│   └── SummaryNode.java               # 요약 & 메타데이터
├── workflow/
│   └── ContentCreationWorkflow.java    # 노드들을 그래프로 조립
├── controller/
│   └── WorkflowController.java         # REST API
└── dto/
    ├── WorkflowRequest.java
    ├── WorkflowResponse.java
    └── StepInfo.java
```

## LangGraph → Spring AI 매핑

| LangGraph (Python) | Spring AI Demo | 비고 |
|---|---|---|
| `StateGraph(State)` | `new StateGraphBuilder()` | 빌더 패턴 |
| `graph.add_node("name", fn)` | `.addNode("name", springBean)` | 노드는 `@Component` 빈 |
| `graph.add_edge("A", "B")` | `.addEdge("A", "B")` | 동일 API |
| `graph.set_entry_point("A")` | `.setEntryPoint("A")` | 동일 API |
| `graph.compile()` | `.compile()` | 실행 가능한 `StateGraph` 반환 |
| `graph.invoke(state)` | `graph.execute(initialState)` | 순차 실행 |

## 파이프라인 흐름

```
input_topic → [TopicAnalysis] → topic_analysis
            → [Research]      → research_points
            → [DraftWriting]  → draft_content
            → [Review]        → review_notes, final_content
            → [Summary]       → summary, completed
```

## 실행 방법

### 1. 환경변수 설정

```bash
export OPENAI_API_KEY=sk-your-key-here
```

### 2. 빌드 & 테스트

```bash
./mvnw test
```

### 3. 서버 실행

```bash
./mvnw spring-boot:run
```

### 4. API 호출

```bash
# 파이프라인 단계 조회
curl http://localhost:8080/api/workflow/content-creation/steps

# 콘텐츠 생성 실행 (30~90초 소요)
curl -X POST http://localhost:8080/api/workflow/content-creation \
  -H "Content-Type: application/json" \
  -d '{"topic": "Spring AI"}'
```

또는 Swagger UI에서 테스트: `http://localhost:8080/swagger-ui.html`

## API

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/workflow/content-creation/steps` | 파이프라인 단계 목록 |
| `POST` | `/api/workflow/content-creation` | 콘텐츠 생성 파이프라인 실행 |

### 요청 예시

```json
{
  "topic": "Spring AI"
}
```

### 응답 예시

```json
{
  "topic": "Spring AI",
  "topicAnalysis": "...",
  "researchPoints": "...",
  "draftContent": "...",
  "reviewNotes": "...",
  "finalContent": "...",
  "summary": "...",
  "executionTrace": [
    "topic_analysis (11453ms)",
    "research (9070ms)",
    "draft_writing (24390ms)",
    "review (33409ms)",
    "summary (2986ms)"
  ]
}
```
