FROM gradle:8.14.5-jdk21-alpine AS builder
WORKDIR /app
COPY app .

RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/install/app ./
EXPOSE 8080
CMD ["./bin/app"]
