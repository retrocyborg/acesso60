# Regras de QA — exigências e diretrizes do Acesso60

## Origem das regras
O enunciado da UC **Qualidade de Software** exige software funcional com algum nível de IA, demonstração de **seis critérios de qualidade**, documentação do processo e uso crítico de IA. A RT03 (28/10/2026) prioriza consolidação, alfa/beta, testes iniciais, validação/tratamento de erros, relatório de IA e roteiro de apresentação. Os identificadores QA abaixo foram propostos para governança do Acesso60, **não são códigos oficiais do professor**.

## Normas e Padrões de Referência (Padrão-Ouro)

O projeto adota como padrão-ouro os referenciais normativos internacionais de engenharia de software e testes:

| Norma / Padrão | Escopo no Acesso60 | Aplicação e Alinhamento |
|---|---|---|
| **ISO 9000 / 2026** | Sistemas de Gestão da Qualidade | Foco no usuário idoso, processos orientados a valor, melhoria contínua e tomada de decisão baseada em evidências auditáveis. |
| **ISO/IEC/IEEE 29119 / 29119-1** | Conceitos e Definições de Teste de Software | Vocabulário padronizado, ciclo de vida de testes e processos de teste de ponta a ponta. |
| **IEEE 829** | Documentação de Testes de Software e Sistema | Estrutura formal para especificação de casos de teste, planos, procedimentos de execução e relatórios de resultados. |
| **IEEE 1012** | Verificação e Validação (V&V) | Rigor formal de V&V por níveis de integridade: nível máximo de rigor para a política de risco e autorização de comandos sensíveis. |
| **IEEE 1028** | Revisões e Auditorias de Software | Protocolos de revisão técnica por pares (pair programming) e auditorias de conformidade de código e documentação. |
| **IEEE 1044** | Classificação de Anomalias de Software | Taxonomia estruturada para identificação, severidade, impacto, priorização e tratamento controlado de falhas/defeitos. |

## Seis critérios de comprovação da disciplina
| ID interno | Critério do enunciado | Padrão-Ouro de Referência | Evidência sugerida |
|---|---|---|---|
| ACAD-Q1 | Definição e rastreabilidade RF/RNF | ISO 9000:2026 / IEEE 1012 | Matriz e histórias com aceite |
| ACAD-Q2 | Padronização e Clean Code | IEEE 1028 (Revisão técnica) | Código modular, formatador e linter |
| ACAD-Q3 | Testes unitários e de integração basais | ISO/IEC/IEEE 29119 / IEEE 829 | Execução pass/fail e cobertura |
| ACAD-Q4 | Validação e tratamento de erros | IEEE 1044 / IEEE 1012 | Entradas inválidas recusadas, erros amigáveis |
| ACAD-Q5 | Usabilidade, interface e responsividade | ISO 9000:2026 (Foco no cliente) | Comparativo protótipo/implementação e navegação |
| ACAD-Q6 | Documentação técnica | IEEE 829 / ISO 9000:2026 | README de build/uso e documentação de interfaces existentes |

## Regras operacionais propostas
| ID | Regra | Evidência aceitável |
|---|---|---|
| QA-01 | Cada funcionalidade tem requisito e aceite rastreáveis | `requirements.md` + teste |
| QA-02 | Separar lógica de negócio de APIs Android | Revisão de módulos e testes unitários |
| QA-03 | Validação antes da execução | Teste de comando inválido |
| QA-04 | Operação sensível não é realizada sem autorização | Teste de bloqueio/confirmar/cancelar |
| QA-05 | Falhas não resultam em ações arbitrárias | Testes negativos |
| QA-06 | Interface nunca apresenta stack traces | Inspeção e teste de erro |
| QA-07 | Mudanças funcionais exigem atualização de testes | Diff e execução |
| QA-08 | Padrões de estilo devem ser verificáveis | Saída de ktlint/detekt/Android Lint, se configurados |
| QA-09 | Ações de acessibilidade respeitam consentimento e permissões | Demonstração e checklist |
| QA-10 | Logs evitam dados sensíveis | Revisão e testes de logging |
| QA-11 | Outputs de IA recebem revisão humana | Relatório de IA com exemplos |
| QA-12 | Critério concluído exige evidência, não declaração | Link de teste, vídeo, relatório ou commit |

## Qualidade da evidência
Registrar data, dispositivo/emulador, versão Android, commit, comando executado, resultado esperado e observado. Distinguir `não executado`, `bloqueado`, `falhou`, `passou`.

## RT03 versus RT04
RT03 pede evidências iniciais de QA; RT04 exige comprovação final dos seis critérios, métricas, relatórios finais, documentação completa e publicação APK/deploy. Não deslocar automaticamente todos os itens da RT04 para o escopo obrigatório da RT03.
