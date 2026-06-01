# 📋 A3 2.0 - Sistema de Gestão de Projetos e Equipes

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Theme](https://img.shields.io/badge/Theme-Dark%20Mode-blueviolet.svg)](#)

Sistema web completo para gerenciamento de projetos, equipes e tarefas com um quadro Kanban interativo. O projeto foi atualizado para as tecnologias mais recentes do ecossistema Java, apresentando uma interface moderna em **Dark Mode** e processamento assíncrono seguro.

---

## ✨ Funcionalidades

- 🌙 **Dark Mode Nativo:** Interface moderna e otimizada para longas jornadas de trabalho.
- 📊 **Dashboard Dinâmico:** Painel inicial com contagem em tempo real de equipes, projetos ativos e tarefas pendentes.
- 🔐 **Segurança Avançada:** Implementação completa com Spring Security 6 e proteção CSRF configurada para requisições AJAX.
- 👥 **Gestão de Usuários:** Controle de perfis (Administrador, Gerente e Colaborador) com permissões granulares.
- 📁 **Gestão de Projetos:** Ciclo de vida completo com controle de status, prazos e gerência associada.
- 🤝 **Gestão de Equipes:** Organização de times com líderes e membros integrados.
- 📋 **Quadro Kanban:** Interface interativa (Drag & Drop pronto) para gestão visual de tarefas.
- 💾 **Banco Embutido (SQLite):** Sem necessidade de instalação de banco de dados externo; pronto para rodar.

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3.2.0
- **Segurança:** Spring Security 6 (com CsrfTokenRequestAttributeHandler)
- **Persistência:** Spring Data JPA + Hibernate 6
- **Banco de Dados:** SQLite

### Frontend
- **Interface:** Thymeleaf + Bootstrap 5.3
- **Ícones:** Bootstrap Icons
- **Estilização:** CSS3 customizado com variáveis dinâmicas (Dark Theme)
- **Comportamento:** JavaScript (ES6) com suporte a Fetch API e CSRF Headers

---

## 🔑 Credenciais de Teste

O sistema conta com um inicializador automático de usuários para facilitar os testes:

| Perfil | Login | Senha |
| :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` |
| **Gerente** | `gerente` | `gerente123` |
| **Colaborador** | `colaborador` | `colab123` |

---

## 📂 Estrutura do Projeto

```text
A3-2.0/
├── src/
│   ├── main/
│   │   ├── java/         # Controllers, Services, Repositories, DTOs (Records)
│   │   └── resources/
│   │       ├── static/   # CSS Global (Dark Mode), JS (UX/Kanban)
│   │       ├── templates/# Fragments e Páginas Thymeleaf
│   │       └── application.properties # Gestão de Profiles (dev/test)
└── pom.xml               # Dependências do Projeto

🚀 Como Executar
1. Pré-requisitos
JDK 21
Maven 3.6+
2. Instalação
code
Bash
git clone https://github.com/sunstrix/A3-2.0.git
cd A3-2.0
3. Execução
Para compilar e rodar a aplicação:
code
Bash
mvn clean compile
mvn spring-boot:run
Acesse: http://localhost:8080
🤝 Contribuição
Faça um Fork do projeto.
Crie uma Branch para sua funcionalidade (git checkout -b feature/NovaFuncionalidade).
Faça o Commit (git commit -m 'Adiciona nova funcionalidade').
Envie para o Push (git push origin feature/NovaFuncionalidade).
Abra um Pull Request.
code
Code
---

### 📝 RESUMO PARA O GITHUB
`docs: atualização completa do README destacando o Java 21, Dark Mode e credenciais de teste`

---

### ✅ CONSIDERAÇÕES FINAIS
Com a atualização do README, encerramos este ciclo de refatoração. O projeto agora está:
1. **Configurado corretamente** para o Spring Boot 3.2/Java 21.
2. **Visualmente atraente** com o Dark Mode padrão.
3. **Funcionalmente robusto**, com erros de template e de lógica corrigidos.
4. **Documentado**, facilitando o uso por terceiros.

**Deseja prosseguir com a refatoração de mais algum módulo ou o projeto está pronto para o seu próximo passo?**