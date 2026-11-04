# Acesso60

**Assistente Android por voz, com foco em acessibilidade e execução controlada de comandos.**

O Acesso60 é um projeto acadêmico de Qualidade de Software. A proposta é ajudar pessoas — incluindo pessoas idosas — a realizar tarefas selecionadas no smartphone por meio de comandos de voz. O projeto prioriza feedback claro, validação das entradas e controle de risco.

## Como funciona

O fluxo previsto para um comando é:

1. O aplicativo reconhece a fala e a converte em texto.
2. O núcleo interpreta e valida o comando.
3. A política de risco decide se a ação pode prosseguir ou exige confirmação.
4. Um adaptador Android executa a ação autorizada.
5. O aplicativo informa o resultado sem apresentar detalhes técnicos ao usuário.

A arquitetura não pretende permitir automação irrestrita. Recursos ainda em planejamento não devem ser tratados como disponíveis.

## Estado do projeto

O projeto está em desenvolvimento. Consulte o [status RT03](docs/milestones/rt03-status.md) e o [roadmap](docs/milestones/roadmap.md) para distinguir o que está planejado, implementado e verificado. A existência de código, por si só, não comprova que uma função foi validada em um dispositivo.

## Por onde começar

- [Visão geral](docs/project/overview.md) — problema, público, objetivo e limites do MVP.
- [Arquitetura](docs/project/architecture.md) — componentes e fluxo técnico.
- [Requisitos](docs/engineering/requirements.md) — comportamentos previstos.
- [Estratégia de testes](docs/engineering/testing-strategy.md) — casos e evidências de qualidade.
- [Rastreabilidade](docs/engineering/traceability.md) — relação entre requisitos, implementação e testes.
- [Plano RT03](docs/milestones/rt03-plan.md) — entregáveis acadêmicos.
- [Status RT03](docs/milestones/rt03-status.md) — evidências e pendências.
- [Índice da documentação](docs/INDEX.md) — mapa dos documentos do projeto.

## Como contribuir

Antes de alterar código ou documentação, leia [Agents.md](Agents.md) e consulte o status e o roadmap. Registre resultados com data, procedimento, resultado observado e limitações. Marque como **A validar** aquilo que ainda não tiver evidência suficiente.
