# 📋 A3 2.0 - Sistema de Gestão, Help Desk e Kanban

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Theme](https://img.shields.io/badge/Theme-Dark%20Mode-blueviolet.svg)](#)
[![Help Desk](https://img.shields.io/badge/Module-Help%20Desk-blue.svg)](#)

Sistema web corporativo completo que unifica a **Gestão de Projetos e Equipes** com um robusto módulo de **Help Desk (Suporte)**. O projeto foi atualizado para as tecnologias mais recentes do ecossistema Java (Java 21 e Spring Boot 3.2), apresentando uma interface moderna em **Dark Mode**, processamento assíncrono de e-mails e banco de dados embutido (SQLite).

---

## ✨ Funcionalidades Principais

### 🎧 Módulo Help Desk (Novo!)
- 🎫 **Gestão de Tickets (Chamados):** Abertura, acompanhamento, atribuição de atendentes e encerramento de chamados.
- 💬 **Timeline de Interações:** Histórico completo de comentários com suporte a **Notas Internas** (restritas à equipe de suporte).
- 📧 **Notificações por E-mail:** Envio automático e assíncrono de e-mails HTML responsivos em cada movimentação do ticket.
- 📚 **Base de Conhecimento (FAQ):** Criação de artigos, tutoriais e documentação com contador de visualizações e busca textual.
- 📊 **Painel do Atendente:** Dashboard com métricas de SLA, filas de atendimento e cartões de status em tempo real.
- 🌐 **Internacionalização (i18n):** Sistema e validações 100% traduzidos para o Português do Brasil (pt_BR).

### 📊 Módulo de Gestão (Original)
- 🌙 **Dark Mode Nativo:** Interface moderna e otimizada para longas jornadas de trabalho.
- 📈 **Dashboard Dinâmico:** Painel inicial com contagem em tempo real de equipes, projetos ativos e tarefas.
- 🔐 **Segurança Avançada:** Spring Security 6 com proteção CSRF e controle de acesso granular.
- 👥 **Gestão de Usuários:** Perfis de Administrador, Gerente e Colaborador.
- 📁 **Gestão de Projetos:** Ciclo de vida completo com controle de status, prazos e escopo.
- 🤝 **Gestão de Equipes:** Organização de times com líderes e membros integrados.
- 📋 **Quadro Kanban:** Interface interativa (Drag & Drop) para gestão visual de tarefas.
- 💾 **Banco Embutido (SQLite):** Armazenamento em arquivo local, zero configuração de SGBD.

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3.2.0
- **Segurança:** Spring Security 6
- **Persistência:** Spring Data JPA + Hibernate 6 (SQLite Dialect)
- **E-mail:** Spring Mail (JavaMailSender) + Thymeleaf (Templates HTML)
- **Validação:** Jakarta Bean Validation (Hibernate Validator)

### Frontend
- **Template Engine:** Thymeleaf + Layout Dialect
- **Framework CSS:** Bootstrap 5.3
- **Ícones:** Bootstrap Icons
- **Estilização:** CSS3 customizado (Variáveis Dark Theme e Timeline)
- **Comportamento:** JavaScript (ES6) com Fetch API

---

## 🔑 Credenciais de Teste (Data Seeder)

O sistema possui um inicializador automático que cria os seguintes usuários no primeiro boot:

| Perfil | Login | Senha | Acesso ao Help Desk |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | Total (Painel do Atendente + Base de Conhecimento) |
| **Gerente** | `gerente` | `gerente123` | Total |
| **Colaborador** | `colaborador` | `colab123` | Solicitante (Abertura e acompanhamento de tickets) |

---

## ⚙️ Configuração do Servidor de E-mail (SMTP)

Para que o sistema de notificações do Help Desk funcione, edite o arquivo `src/main/resources/application.properties` e insira as credenciais do seu servidor SMTP (exemplo com Gmail):

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-de-app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```
*(Nota: Se o seu provedor usar autenticação de dois fatores, gere uma "Senha de App" específica para usar no campo password).*

---

## 🚀 Como Executar

### 1. Pré-requisitos
- JDK 21 (Recomendado: Eclipse Temurin)
- Maven 3.6+
- Git

### 2. Instalação Automatizada (Windows)
Se você estiver no Windows 10/11, basta executar o script `instalar.bat` na raiz do projeto. Ele verificará e instalará o Git, Java 21 e Maven automaticamente via `winget`, gerando um log de instalação.

### 3. Clonagem e Execução Manual
```bash
git clone https://github.com/sunstrix/A3-2.0.git
cd A3-2.0

# Compilar o projeto
mvn clean install

# Iniciar a aplicação
mvn spring-boot:run
```

Acesse a aplicação no seu navegador: **[http://localhost:8080](http://localhost:8080)**

---

## 📂 Estrutura do Projeto (Atualizada)

```text
A3-2.0/
├── src/
│   ├── main/
│   │   ├── java/br/com/projetoA3/
│   │   │   ├── config/       # MessageConfig (i18n e Validacoes)
│   │   │   ├── controller/   # Controllers (Gestao + Help Desk)
│   │   │   ├── model/        # Entidades JPA (Ticket, Artigo, etc.)
│   │   │   ├── repository/   # Interfaces Spring Data JPA
│   │   │   └── service/      # Regras de Negocio e EmailService
│   │   └── resources/
│   │       ├── static/css/   # Estilos Customizados e Dark Mode
│   │       ├── templates/    # Thymeleaf (Layouts, Emails, Tickets, KB)
│   │       ├── application.properties
│   │       └── messages*.properties # Dicionarios i18n PT-BR
├── instalar.bat              # Script de setup automatizado (Windows)
├── .gitignore
└── pom.xml                   # Dependencias do Maven
```

---

## 🤝 Contribuição

1. Faça um Fork do projeto.
2. Crie uma Branch para sua funcionalidade (`git checkout -b feature/NovaFuncionalidade`).
3. Faça o Commit de suas alterações (`git commit -m 'Adiciona nova funcionalidade'`).
4. Envie para o Push (`git push origin feature/NovaFuncionalidade`).
5. Abra um Pull Request.

---

**Desenvolvido com ☕ Java e 🍃 Spring Boot.**