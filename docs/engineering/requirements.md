# Requisitos do Acesso60 — catálogo de trabalho

> **Aviso:** este catálogo consolida o comportamento discutido; os IDs `A60-RF-*` são identificadores internos desta documentação, **não substituem** a numeração formal original da RT02. Antes de sincronizar com GitBook ou Jira, confrontar a matriz oficial.

## Funcionais
| ID interno | Requisito resumido | Aceite proposto | Estado de implementação |
|---|---|---|---|
| A60-RF-01 | Iniciar escuta mediante ação do usuário | Indicar início/cancelamento da escuta | Não verificado |
| A60-RF-02 | Reconhecer fala | Retornar texto ou erro estruturado | Não verificado |
| A60-RF-03 | Interpretar comandos apoiados | Mapear “voltar”, “início” e variações definidas | Não verificado |
| A60-RF-04 | Validar comandos | Rejeitar vazio/desconhecido/parâmetros faltantes | Não verificado |
| A60-RF-05 | Classificar risco | Retornar categoria/política | Não verificado |
| A60-RF-06 | Confirmar ação sensível | Não executar sem aprovação | Não verificado |
| A60-RF-07 | Rotear comando permitido | Selecionar executor compatível | Não verificado |
| A60-RF-08 | Navegar HOME/BACK | Executar via API Android apropriada | Não verificado |
| A60-RF-09 | Abrir aplicativo | Tratar app ausente/não acessível | Não verificado |
| A60-RF-10 | Alterar volume | Tratar limites e indisponibilidade | Não verificado |
| A60-RF-11 | Ler conteúdo acessível | Só leitura disponível e autorizada, com privacidade | Não verificado |
| A60-RF-12 | Informar resultado | Feedback de sucesso, erro ou cancelamento | Não verificado |
| A60-RF-13 | Recuperar falha de interpretação | Mensagem de repetição sem execução | Não verificado |

## Não funcionais e restrições
| ID | Requisito de referência | Verificação proposta |
|---|---|---|
| A60-RNF-01 | Interface acessível e legível | Teste visual, semântica Compose, inspeção |
| A60-RNF-02 | Lógica testável e modular | Testes de domínio com fakes |
| A60-RNF-03 | Tratamento de erros sem stack trace na UI | Casos negativos |
| A60-RNF-04 | Privacidade de áudio/texto/tela | Revisão de permissões e logs |
| A60-RNF-05 | Desempenho adequado | Instrumentação e meta aprovada posteriormente |
| A60-RNF-06 | Comando não autorizado não produz efeito | Casos de autorização |

## Rastreabilidade inicial
| Fluxo | Requisitos | Componentes | Teste-base |
|---|---|---|---|
| Reconhecer “voltar” | RF-01/02/03 | STT, Interpreter | STT-01, INT-01 |
| Executar “voltar” | RF-04/05/07/08/12 | Validator, Risk, Router, Executor | E2E-01 |
| Rejeitar comando desconhecido | RF-03/04/13 | Interpreter, Validator, Feedback | INT-03, E2E-03 |
| Confirmar ação sensível simulada | RF-05/06/12 | Risk, confirmação UI | RISK-02, E2E-02 |

## Prioridade MVP
P0: escuta, interpretação, validação, HOME/BACK, feedback, erros e testes básicos. P1: abertura de app, confirmação demonstrável, volume, CI. P2: leitura de tela mais ampla, preferências e fallback LLM — conforme necessidade e prazo.

## Pendências de requisitos
Verificar IDs e texto integral RF/RNF na versão aprovada da RT02, metas mensuráveis, riscos de acesso a aplicativos de terceiros e escopo de IA no produto.
