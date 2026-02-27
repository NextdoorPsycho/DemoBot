FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system demobot && useradd --system --gid demobot demobot

COPY build/libs/ArcaneDemoBot-*.jar /app/ArcaneDemoBot.jar
COPY config/bot.toml.example /app/config/bot.toml.example

RUN mkdir -p /app/data /app/config && chown -R demobot:demobot /app

USER demobot

VOLUME ["/app/data", "/app/config"]

ENTRYPOINT ["java", "-jar", "/app/ArcaneDemoBot.jar"]
