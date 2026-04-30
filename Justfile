set shell := ["bash", "-uc"]

# Migrate local DB
migrate *args:
    dotenvx run -- ./mvnw flyway:migrate -Dflyway.locations=filesystem:./db {{ args }}

# Drop local DB
drop *args:
    dotenvx run -- ./mvnw flyway:clean -Dflyway.locations=filesystem:./db -Dflyway.cleanDisabled=false {{ args }}
