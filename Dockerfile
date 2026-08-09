# ---------- Этап сборки ----------
# Собираем jar-файл проекта с помощью Maven и Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала копируем только pom.xml — так Docker закэширует загруженные зависимости
# и при последующих сборках не будет скачивать их заново, если pom.xml не менялся.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Теперь копируем весь код и собираем jar (тесты пропускаем — их тут нет)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Этап запуска ----------
# Берём только готовый jar, без Maven и лишних инструментов — контейнер лёгкий и быстрый
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render сам передаёt порт через переменную окружения PORT — приложение уже настроено
# слушать именно её (см. application.properties: server.port=${PORT:8080})
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
