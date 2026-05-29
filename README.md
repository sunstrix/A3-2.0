

```markdown
# 📊 A3 2.0 - Sistema de Gestão de Projetos e Equipes

Sistema web completo para gerenciamento de projetos, equipes e tarefas com quadro Kanban interativo, desenvolvido em **Java Spring Boot 3.2** com banco de dados **SQLite**.

## 📋 Índice

1. [Pré-requisitos](#-pré-requisitos)
2. [Instalação do Java](#-instalação-do-java)
3. [Instalação do Maven](#-instalação-do-maven)
4. [Download do Projeto](#-download-do-projeto)
5. [Instalação das Dependências](#-instalação-das-dependências)
6. [Execução do Projeto](#-execução-do-projeto)
7. [Acesso ao Sistema](#-acesso-ao-sistema)
8. [Solução de Problemas](#-solução-de-problemas)

---

## 🔧 Pré-requisitos

Para executar este projeto, você precisará instalar:

- ✅ **Java Development Kit (JDK) 21 ou superior**
- ✅ **Apache Maven 3.6+**
- ✅ **Git** (opcional, para clonar o repositório)

---

## 1️⃣ Instalação do Java

### Windows

1. **Baixe o JDK 21:**
   - Acesse: https://adoptium.net/
   - Clique em "Latest LTS Release"
   - Baixe a versão para Windows (arquivo `.msi`)

2. **Instale o JDK:**
   - Execute o arquivo baixado
   - Clique em "Next" até concluir
   - **IMPORTANTE:** Marque a opção "Add to PATH" durante a instalação

3. **Verifique a instalação:**
   - Abra o PowerShell (tecla Windows + X → PowerShell)
   - Digite:
     ```powershell
     java -version
     ```
   - Deve aparecer algo como: `openjdk version "21.x.x"`

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### macOS

```bash
brew install openjdk@21
java -version
```

---

## 2️⃣ Instalação do Maven

### Windows

1. **Baixe o Maven:**
   - Acesse: https://maven.apache.org/download.cgi
   - Baixe o arquivo `apache-maven-3.9.x-bin.zip`

2. **Extraia o arquivo:**
   - Extraia em `C:\Program Files\Apache\maven`

3. **Configure as Variáveis de Ambiente:**
   - Pressione `Windows + R`
   - Digite `sysdm.cpl` e pressione Enter
   - Clique em "Variáveis de Ambiente"
   - Em "Variáveis do Sistema", clique em "Novo":
     - Nome: `MAVEN_HOME`
     - Valor: `C:\Program Files\Apache\maven`
   - Encontre a variável `Path`, clique em "Editar"
   - Clique em "Novo" e adicione: `%MAVEN_HOME%\bin`
   - Clique em OK em todas as janelas

4. **Verifique a instalação:**
   - Feche e abra o PowerShell novamente
   - Digite:
     ```powershell
     mvn -version
     ```
   - Deve aparecer a versão do Maven e do Java

### Linux/macOS

```bash
# Ubuntu/Debian
sudo apt install maven

# macOS (com Homebrew)
brew install maven

# Verifique
mvn -version
```

---

## 3️⃣ Download do Projeto

### Opção A: Via Git (Recomendado)

1. **Instale o Git** (se ainda não tiver):
   - Windows: https://git-scm.com/download/win
   - Execute o instalador e clique em "Next" até concluir

2. **Clone o repositório:**
   ```powershell
   # Crie uma pasta para seus projetos
   mkdir C:\projetos
   cd C:\projetos
   
   # Clone o projeto
   git clone https://github.com/seu-usuario/a3-2.0.git
   
   # Entre na pasta do projeto
   cd a3-2.0
   ```

### Opção B: Download Direto

1. Acesse o repositório no GitHub
2. Clique no botão verde "Code"
3. Clique em "Download ZIP"
4. Extraia o arquivo ZIP em uma pasta (ex: `C:\projetos\a3-2.0`)
5. Abra o PowerShell nesta pasta

---

## 4️⃣ Instalação das Dependências

O Maven vai baixar automaticamente todas as bibliotecas necessárias.

1. **Abra o PowerShell na pasta do projeto:**
   - Navegue até a pasta `a3-2.0`
   - Segure `Shift` e clique com botão direito
   - Selecione "Abrir janela do PowerShell aqui"

2. **Execute o comando:**
   ```powershell
   mvn clean install
   ```

3. **Aguarde o download:**
   - Primeira execução: 10-15 minutos (dependendo da internet)
   - O Maven vai baixar ~100MB de bibliotecas
   - Você verá mensagens de download no terminal

4. **Verifique se funcionou:**
   - No final, deve aparecer: `BUILD SUCCESS`
   - Se aparecer erro, verifique sua conexão com a internet

---

## 5️⃣ Execução do Projeto

### Método 1: Via Maven (Recomendado)

1. **No PowerShell, execute:**
   ```powershell
   mvn spring-boot:run
   ```

2. **Aguarde a inicialização:**
   - O sistema vai compilar e iniciar
   - Aguarde até aparecer:
     ```
     Started A3Application in X.XX seconds
     Tomcat started on port(s): 8080 (http)
     ```

3. **O banco de dados será criado automaticamente:**
   - Arquivo: `a3_2_0.db` (na pasta do projeto)
   - Tabelas criadas automaticamente
   - Usuários de teste inseridos automaticamente

### Método 2: Gerar e Executar JAR

1. **Compile o projeto:**
   ```powershell
   mvn clean package
   ```

2. **Execute o JAR:**
   ```powershell
   java -jar target/a3-2-0-0.0.1-SNAPSHOT.jar
   ```

---

## 6️⃣ Acesso ao Sistema

1. **Abra seu navegador** (Chrome, Firefox, Edge)

2. **Acesse:**
   ```
   http://localhost:8080
   ```

3. **Faça login com um dos usuários:**

   | Perfil        | Login         | Senha      |
   |---------------|---------------|------------|
   | Administrador | `admin`       | `admin`    |
   | Gerente       | `gerente`     | `gerente`  |
   | Colaborador   | `colaborador` | `123456`   |

4. **Pronto!** O sistema está funcionando ✅

---

## 🐛 Solução de Problemas

### Erro: "java não é reconhecido"

**Problema:** Java não está no PATH

**Solução:**
1. Verifique se instalou o Java corretamente
2. Reinicie o computador após instalar
3. Abra um NOVO PowerShell (não o antigo)
4. Execute: `java -version`

### Erro: "mvn não é reconhecido"

**Problema:** Maven não está configurado

**Solução:**
1. Verifique se criou a variável `MAVEN_HOME`
2. Verifique se adicionou `%MAVEN_HOME%\bin` no PATH
3. Reinicie o PowerShell
4. Execute: `mvn -version`

### Erro: "Port 8080 already in use"

**Problema:** Outra aplicação está usando a porta 8080

**Solução 1 - Matar o processo:**
```powershell
# Descubra o PID
netstat -ano | findstr :8080

# Mate o processo (substitua XXXXX pelo PID)
taskkill /PID XXXXX /F
```

**Solução 2 - Mudar a porta:**
1. Abra `src/main/resources/application.properties`
2. Adicione ou altere:
   ```properties
   server.port=8081
   ```
3. Execute novamente

### Erro: "Ambiguous mapping"

**Problema:** Conflito de rotas no código

**Solução:**
Este erro já foi corrigido na versão atual. Se aparecer:
1. Verifique se há métodos duplicados nos controllers
2. Execute `mvn clean` antes de rodar novamente

### Erro: "BUILD FAILURE"

**Problema:** Falha na compilação

**Solução:**
1. Verifique sua conexão com a internet
2. Execute:
   ```powershell
   mvn clean
   mvn install
   ```
3. Se persistir, delete a pasta `.m2` em `C:\Users\seu-usuario\.m2`

### O sistema não abre no navegador

**Solução:**
1. Verifique se apareceu "Started A3Application" no terminal
2. Aguarde alguns segundos após a mensagem
3. Tente acessar: `http://localhost:8080/login`
4. Verifique se não há firewall bloqueando

---

## 📁 Estrutura de Pastas

Após a execução, você terá:

```
a3-2.0/
├── src/                          # Código fonte
├── target/                       # Arquivos compilados (gerado automaticamente)
├── a3_2_0.db                     # Banco de dados SQLite (gerado na 1ª execução)
├── pom.xml                       # Configuração do Maven
└── README.md                     # Este arquivo
```

---

## 🔄 Como Parar e Iniciar Novamente

### Parar o sistema:
- No PowerShell, pressione: `Ctrl + C`

### Iniciar novamente:
```powershell
mvn spring-boot:run
```

### Limpar e reiniciar (se necessário):
```powershell
mvn clean
mvn spring-boot:run
```

---

## 📝 Notas Importantes

- **Primeira execução:** Pode demorar 10-15 minutos para baixar todas as dependências
- **Conexão com internet:** Necessária apenas na primeira execução (para download das bibliotecas)
- **Banco de dados:** SQLite é criado automaticamente - não precisa instalar nada
- **Senhas:** Estão em texto puro para desenvolvimento (NÃO use em produção)
- **Dados de teste:** São inseridos automaticamente apenas na primeira execução

---

## 🎯 Funcionalidades

Após o login, você poderá:

- 👥 **Gerenciar Usuários:** Criar, editar e excluir usuários
- 📁 **Gerenciar Projetos:** Criar projetos com datas e status
- 🤝 **Gerenciar Equipes:** Criar equipes com líderes e membros
- 📋 **Quadro Kanban:** Visualizar e mover tarefas entre colunas
- 🔍 **Relatórios:** Acompanhar progresso dos projetos

---

## 💻 Suporte

Em caso de dúvidas ou problemas:

1. Verifique a seção "Solução de Problemas" acima
2. Verifique se seguiu todos os passos corretamente
3. Consulte os logs no PowerShell para mensagens de erro

---

**Desenvolvido por:** Alex Paulo  
**Versão:** 1.0.0  
**Data:** Maio/2026  
**Status:** ✅ Funcional e Pronto para Uso

---

⭐ **Se o projeto funcionou, não esqueça de dar uma estrela no GitHub!**
```

---

Este README está **super completo** e explica **passo a passo** como qualquer pessoa, mesmo sem experiência, pode instalar e executar o projeto do zero. Ele inclui:

✅ Instalação do Java (Windows, Linux, macOS)  
✅ Instalação do Maven com configuração de PATH  
✅ Download via Git ou ZIP  
✅ Instalação das dependências  
✅ Execução do projeto  
✅ Acesso ao sistema  
✅ Solução de problemas comuns  
✅ Estrutura de pastas  
✅ Como parar/iniciar  
✅ Notas importantes  

Agora é só salvar como `README.md` na raiz do projeto! 🚀
