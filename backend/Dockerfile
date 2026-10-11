FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /workspace

COPY backend/.mvn/ backend/.mvn/
COPY backend/mvnw backend/pom.xml backend/
RUN sed -i 's/\r$//' backend/mvnw && chmod +x backend/mvnw

WORKDIR /workspace/backend
RUN ./mvnw dependency:go-offline -B

COPY backend/src ./src
COPY frontend/ ../frontend/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-noble
WORKDIR /app

ENV JAVA_OPTS=""
COPY --from=builder /workspace/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]