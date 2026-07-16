# ── Stage 1: React 빌드 ──────────────────────────────
FROM node:20-alpine AS frontend
WORKDIR /frontend
# npm을 로컬 개발환경과 같은 11로 맞춤 (node:20-alpine 기본 npm 10은
# npm 11이 생성한 lockfile을 out-of-sync로 오판해 npm ci가 실패함)
RUN npm install -g npm@11
# frontend 전체를 복사 (package-lock.json 포함 보장 → npm ci 안정)
COPY frontend/ ./
RUN npm ci
# CI=false: react-scripts가 ESLint 경고를 에러로 취급하지 않도록
RUN CI=false npm run build

# ── Stage 2: Spring jar 빌드 (React 빌드본을 static에 포함) ──
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY . .
# Stage 1의 React 빌드 결과를 Spring 정적 리소스로 복사
COPY --from=frontend /frontend/build ./src/main/resources/static
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# ── Stage 3: 실행 (경량 JRE) ──────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
