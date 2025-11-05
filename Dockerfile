# Используем универсальный базовый образ с JDK
FROM eclipse-temurin:17-jdk

# Указываем рабочую директорию приложения
WORKDIR /app

# Добавляем данные в контейнер
ADD . /app

# Устанавливаем необходимые системные пакеты
RUN apt-get update && apt-get install -y \
    maven \
    curl \
    vim \
    net-tools \
    postgresql-client

# Собираем проект прямо внутри контейнера
RUN mvn -B package -DskipTests

# Указываем переменные окружения для БД и приложения
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/app \
    SPRING_DATASOURCE_USERNAME=app \
    SPRING_DATASOURCE_PASSWORD=app \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xmx2g -Xms512m"

# Копируем собранный jar (он уже внутри /app/target после сборки)
RUN cp target/*.jar app.jar

# Добавляем healthcheck
HEALTHCHECK --interval=30s --timeout=5s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Открываем стандартные порты
EXPOSE 8080 5432 80 443

# Устанавливаем переменную PATH для Java
ENV PATH="/usr/local/bin:$PATH"

# Запускаем приложение
CMD java $JAVA_OPTS -jar app.jar
