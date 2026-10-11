# ==============================================================================
# Build Stage
# ==============================================================================
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

# Copy Maven wrapper and POM first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings and ensure execution permissions for the wrapper
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build production JAR without running integration tests during container image build
RUN ./mvnw clean package -DskipTests

# ==============================================================================
# Runtime Stage
# ==============================================================================
FROM eclipse-temurin:25-jre-noble AS runner
WORKDIR /app

# Create a secure non-root user and group
RUN groupadd -r parkease && useradd -r -g parkease parkease

# Environment configurations with sensible production defaults
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R parkease:parkease /app

USER parkease

EXPOSE 8080

# Health check to monitor application health
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --spider --quiet http://localhost:${PORT}/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
