# Índice documental do Acesso60

## Para agentes
1. Leia `../AGENTS.md` para regras de trabalho.
2. Consulte apenas os arquivos relacionados à tarefa.
3. Consulte `milestones/rt03-status.md` antes de declarar qualquer coisa concluída.

## Projeto
- [`project/overview.md`](project/overview.md): problema, público, valor, limites.
- [`project/architecture.md`](project/architecture.md): pipeline de voz, componentes e invariantes.
- [`project/m_docs.md`](project/m_docs.md): arquitetura em camadas, extensibilidade por plugins e suíte de testes baseada na proposta GitBook.
- [`project/development-history.md`](project/development-history.md): decisões e histórico contextual.

## Padrões
- [`standards/qa-rules.md`](standards/qa-rules.md): seis critérios acadêmicos e regras QA propostas.
- [`standards/software-principles.md`](standards/software-principles.md): SOLID, Clean Code, KISS, DRY, YAGNI.
- [`standards/quality-attributes.md`](standards/quality-attributes.md): atributos de produto e possíveis métricas.

## Engenharia
- [`engineering/requirements.md`](engineering/requirements.md): catálogo de comportamentos e rastreabilidade inicial.
- [`engineering/tooling.md`](engineering/tooling.md): stack e ferramentas selecionadas, status não verificado.
- [`engineering/testing-strategy.md`](engineering/testing-strategy.md): casos de teste e evidências.
- [`engineering/traceability.md`](engineering/traceability.md): matriz de requisitos, histórias, testes e evidências.
- [`engineering/android-test-plan.md`](engineering/android-test-plan.md): plano de testes de usabilidade e integração Android.

## Entregas
- [`milestones/rt03-plan.md`](milestones/rt03-plan.md): escopo e entregáveis RT03.
- [`milestones/rt03-status.md`](milestones/rt03-status.md): status com evidências e pendências.
- [`milestones/roadmap.md`](milestones/roadmap.md): roadmap com priorização P0, P1 e P2.

## Planos de Implementação
- [`plans/plano_implementacao_nucleo_beta.md`](plans/plano_implementacao_nucleo_beta.md): plano de arquitetura e implementação do Núcleo Beta P0.
- [`plans/plano_padrao_ouro_normas_qualidade.md`](plans/plano_padrao_ouro_normas_qualidade.md): plano de adoção de normas internacionais de teste e qualidade.
- [`plans/plano_incorporacao_planos_logs_walkthroughs.md`](plans/plano_incorporacao_planos_logs_walkthroughs.md): plano de migração de artefatos da sessão para o repositório.

## Walkthroughs e Validações
- [`walkthroughs/walkthrough_nucleo_beta.md`](walkthroughs/walkthrough_nucleo_beta.md): entrega do Núcleo Beta e suíte de 13 testes unitários e de integração.
- [`walkthroughs/walkthrough_padrao_ouro.md`](walkthroughs/walkthrough_padrao_ouro.md): formalização do padrão-ouro na governança e estratégia de testes.

## Relatórios
- [`reports/estado-real-rt03-2026-10-27.md`](reports/estado-real-rt03-2026-10-27.md): auditoria do estado real após a integração de `RT03_P`.

## Logs de Auditoria e Execução
- [`logs/task-84_download_gradle.log`](logs/task-84_download_gradle.log): log do download e extração do Gradle 8.14.6.
- [`logs/task-96_wrapper_init.log`](logs/task-96_wrapper_init.log): log da inicialização do Gradle Wrapper oficial.
- [`logs/task-102_initial_test_check.log`](logs/task-102_initial_test_check.log): log da primeira checagem de execução com gradlew.
- [`logs/task-229_jvm_compatibility_error.log`](logs/task-229_jvm_compatibility_error.log): log de erro inicial de JVM target (IEEE 1044).
- [`logs/task-241_test_suite_success.log`](logs/task-241_test_suite_success.log): log de execução bem-sucedida da suíte completa de testes.

## Taxonomia de certeza
- **Oficial**: exigência expressa do enunciado acadêmico citado no contexto.
- **Decidido/planejado**: escolha registrada nas conversas; não implica implementação.
- **Implementado**: código encontrado no repositório, ainda sem evidência completa de funcionamento.
- **Verificado**: funcionalidade ou ferramenta executada com evidência registrada.
- **Desconhecido**: não é possível estabelecer situação atual sem inspecionar código e/ou documentação.

Não modificar o histórico para simular conclusão. Registrar data, evidência e fonte quando um status mudar.
