set shell := ["bash", "-uc"]

# Migrate local DB
migrate *args:
    dotenvx run -- ./mvnw flyway:migrate -Dflyway.locations=filesystem:./db {{ args }}

# Drop local DB
drop *args:
    dotenvx run -- ./mvnw flyway:clean -Dflyway.locations=filesystem:./db -Dflyway.cleanDisabled=false {{ args }}

dev *args:
    mvn spring-boot:run

test *args:
    ./mvnw test

# Docker
docker-build:
    docker build -t ghostwriter .

docker-run:
    docker run --env-file .env.docker -p 8080:8080 ghostwriter

# Start app + ngrok via docker compose
docker-up:
    docker compose --env-file .env.docker up --build

docker-down:
    docker compose down
