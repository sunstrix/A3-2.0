# 1. Etapa de Construção (Usando Maven com Java 21)
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
# Baixa dependências e cria o pacote .jar
RUN mvn package -DskipTests && rm -rf /workspace/app/target/*.jar.original

# 2. Etapa de Execução (Usando Java 21 JRE leve)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Cria a pasta onde o banco SQLite vai ser salvo
RUN mkdir -p /data

# Copia o arquivo gerado na etapa anterior
COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]