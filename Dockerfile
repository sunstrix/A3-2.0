# =====================================================
# A3-2.0 - Dockerfile com persistência SQLite correta
# =====================================================

# Usa imagem oficial do Eclipse Temurin (OpenJDK) com Java 21
FROM eclipse-temurin:21-jre-alpine

# Define diretório de trabalho
WORKDIR /app

# Copia o JAR compilado para dentro do container
COPY target/*.jar app.jar

# ✅ CRÍTICO: Cria pasta para o banco SQLite com permissões adequadas
RUN mkdir -p /data && chmod 755 /data

# ✅ CRÍTICO: Define variável de ambiente para URL do banco com WAL mode
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/a3_projeto.db?journal_mode=WAL&synchronous=NORMAL&foreign_keys=ON

# ✅ CRÍTICO: Monta volume persistente para o banco de dados
# Os dados em /data sobreviverão a reinicializações do container
VOLUME ["/data"]

# Expõe a porta da aplicação
EXPOSE 8080

# Comando para executar a aplicação
# A JVM é configurada para containers com -XX:+UseContainerSupport
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]