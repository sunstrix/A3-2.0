#  A3 2.0 - Sistema de Gestão de Projetos e Equipes

Sistema web completo para gerenciamento de projetos, equipes e tarefas com quadro Kanban interativo, desenvolvido em **Java Spring Boot 3.2** com banco de dados **SQLite**.

## ✨ Funcionalidades

- 🔐 **Autenticação e Autorização** com Spring Security
-  **Gestão de Usuários** com perfis (Administrador, Gerente, Colaborador)
- 📁 **Gestão de Projetos** com controle de status e datas
- 🤝 **Gestão de Equipes** com líderes e membros
- 📋 **Quadro Kanban** visual para acompanhamento de tarefas
- 💾 **Banco SQLite** - sem necessidade de instalação de SGBD

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21** ou superior
- **Spring Boot 3.2.0**
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Validation
  - Spring Thymeleaf
- **Maven** (Gerenciamento de dependências)
- **Hibernate** (ORM)
- **SQLite** (Banco de dados)

### Frontend
- **Thymeleaf** (Template engine)
- **Bootstrap 5.3** (Framework CSS)
- **Bootstrap Icons**
- **HTML5 / CSS3 / JavaScript**

##  Pré-requisitos

Antes de começar, você precisa ter instalado:

1. **Java Development Kit (JDK) 21 ou superior**
   - Download: https://adoptium.net/
   - Verifique a instalação:
     ```bash
     java -version
     ```

2. **Apache Maven 3.6+**
   - Download: https://maven.apache.org/download.cgi
   - Verifique a instalação:
     ```bash
     mvn -version
     ```

3. **Git** (opcional, para clonar o repositório)
   - Download: https://git-scm.com/

## 📥 Instalação e Configuração

### 1. Baixar o Projeto

```bash
# Via Git
git clone https://github.com/seu-usuario/a3-2.0.git
cd a3-2.0

# Ou navegue até a pasta se já tiver extraído
cd C:\Users\AlexPaulo\Desktop\A3-2.0
