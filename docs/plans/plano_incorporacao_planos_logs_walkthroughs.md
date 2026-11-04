# Plano de Incorporação — Planos de Implementação, Walkthroughs e Logs no Repositório

## Objetivo
Transferir os artefatos gerados durante as sessões de trabalho (planos de implementação, relatórios de walkthrough e logs de tarefas de build/testes) da pasta temporária da sessão (`brain/`) para a estrutura de documentação versionada do repositório [`docs/`](file:///C:/Prog/acesso60/docs/), em estrita conformidade com as regras de estrutura do [`Agents.md`](file:///C:/Prog/acesso60/AGENTS.md).

---

## User Review Required

> [!IMPORTANT]
> **Estrutura de Subpastas em `docs/`**:
> Para organizar os arquivos sem poluir a raiz de `docs/`, propõe-se criar três subpastas especializadas:
> - [`docs/plans/`](file:///C:/Prog/acesso60/docs/plans/): Planos de implementação técnicos (`.md`).
> - [`docs/walkthroughs/`](file:///C:/Prog/acesso60/docs/walkthroughs/): Relatórios de walkthrough e validação de entregas (`.md`).
> - [`docs/logs/`](file:///C:/Prog/acesso60/docs/logs/): Logs de auditoria e execução de tarefas de compilação/testes (`.log` / `.md`).

> [!NOTE]
> Todos os arquivos transferidos serão catalogados no índice mestre [`docs/INDEX.md`](file:///C:/Prog/acesso60/docs/INDEX.md) para garantir rastreabilidade completa.

---

## Proposed Changes

### Documentação Técnica e Planos (`docs/plans/`)

#### [NEW] `docs/plans/plano_implementacao_nucleo_beta.md`
Cópia versionada do plano técnico de arquitetura e implementação do Núcleo Beta P0.

#### [NEW] `docs/plans/plano_padrao_ouro_normas_qualidade.md`
Cópia versionada do plano de incorporação dos padrões internacionais (ISO/IEC/IEEE 29119, IEEE 829, 1012, 1028, 1044 e ISO 9000:2026).

---

### Relatórios de Validação e Walkthroughs (`docs/walkthroughs/`)

#### [NEW] `docs/walkthroughs/walkthrough_nucleo_beta.md`
Registro da entrega do Núcleo Beta, detalhes das classes e evidências dos 13 testes unitários e de integração aprovados.

#### [NEW] `docs/walkthroughs/walkthrough_padrao_ouro.md`
Registro das alterações de incorporação das normas de engenharia e QA.

---

### Registros de Execução e Logs (`docs/logs/`)

#### [NEW] `docs/logs/task-84_download_gradle.log`
Log da extração e inicialização do Gradle 8.14.6 no ambiente Windows.

#### [NEW] `docs/logs/task-96_wrapper_init.log`
Log de criação e inicialização dos scripts e binários do Gradle Wrapper.

#### [NEW] `docs/logs/task-102_initial_test_check.log`
Log da primeira execução de teste vazia (`NO-SOURCE`).

#### [NEW] `docs/logs/task-229_jvm_compatibility_error.log`
Log do erro de validação de JVM target (Java 24 vs Kotlin JVM 22), preservado como evidência de rastreabilidade de defeitos/anomalias (IEEE 1044).

#### [NEW] `docs/logs/task-241_test_suite_success.log`
Log da execução com sucesso da suíte de 13 testes do Núcleo Beta (`BUILD SUCCESSFUL in 40s`).

---

### Atualização do Índice Mestre (`docs/INDEX.md`)

#### [MODIFY] `docs/INDEX.md`
Adicionar os links para as novas seções de **Planos de Implementação**, **Walkthroughs** e **Logs de Auditoria**.

---

## Verification Plan

### Manual Verification
1. Verificar a criação das subpastas `docs/plans/`, `docs/walkthroughs/` e `docs/logs/`.
2. Verificar a integridade dos arquivos copiados.
3. Validar os links em `docs/INDEX.md`.
4. Executar `git status` para confirmar que a árvore de diretórios obedece às regras do `Agents.md`.
