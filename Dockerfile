# 1. Etapa de Construção (Usando Java 21)
FROM eclipse-temurin:21-jdk-focal AS builder
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
# Baixa dependências e cria o pacote .jar
RUN mvn package -DskipTests && rm -rf /workspace/app/target/*.jar.original

# 2. Etapa de Execução (Usando Java 21 JRE leve)
FROM eclipse-temurin:21-jre-focal
WORKDIR /app

# Copia o arquivo gerado na etapa anterior
COPY --from=builder /workspace/app/target/*.jar app.jar

# Cria a pasta onde o banco SQLite vai ser salvo
RUN mkdir -p /data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]