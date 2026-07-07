FROM openjdk:21-ea-21-jdk-buster

ARG USER_ID=1000
ARG GROUP_ID=1000

RUN groupadd -g ${GROUP_ID} appuser \
    && useradd -u ${USER_ID} -g appuser -m appuser

WORKDIR /app
RUN chown appuser:appuser /app

COPY --chown=appuser:appuser gradle/ gradle/
COPY --chown=appuser:appuser gradlew build.gradle ./

USER appuser

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

CMD ["./gradlew", "bootRun", "--no-daemon"]
