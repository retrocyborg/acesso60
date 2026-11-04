# Visão geral do projeto — Acesso60

**Natureza:** projeto acadêmico de Qualidade de Software, aplicativo Android assistivo.
**Público-alvo principal:** pessoas idosas que desejam interação por voz com funções selecionadas do smartphone.
**Escopo geral:** acessibilidade digital e assistência à navegação e ações controladas.

## Problema
Fluxos convencionais de smartphones podem exigir leitura de textos pequenos, familiaridade com ícones, diversos toques e navegação complexa. Essas barreiras afetam uma parcela dos usuários idosos, sem implicar que todas as pessoas dessa faixa etária tenham as mesmas necessidades.

## Proposta
Oferecer comandos de voz para tarefas definidas; converter fala em texto; interpretar e validar a solicitação; avaliar risco; executar ações permitidas; verificar o resultado quando possível; informar sucesso ou falha de modo claro.

## Objetivo geral
Desenvolver e avaliar um MVP Android que demonstre o pipeline seguro de interpretação e execução por voz, com qualidade verificável e documentação de uso de IA.

## Funcionalidades consideradas
| Função | Exemplo | Condição atual |
|---|---|---|
| Navegação | “voltar”, “início” | BACK e HOME têm adaptadores Android; validação ponta a ponta por voz permanece **A validar**. |
| Abertura de apps | “abrir WhatsApp” | Implementada para allowlist WhatsApp e com confirmação; sucesso observável no app externo permanece **A validar**. |
| Volume | “aumentar volume” | Comandos existem no núcleo, mas não há executor Android integrado. |
| Leitura acessível | “ler a tela” | Comando existe no núcleo, sem executor; permissões e privacidade permanecem **A validar**. |
| Confirmação de risco | “abrir WhatsApp” | O controlador solicita confirmação antes de executar comandos classificados como sensíveis. |
| Recuperação de erro | comando desconhecido ou falha de serviço | A UI possui estados de falha e mensagens amigáveis; cobertura em uso real permanece **A validar**. |
| Widget | atalho “Toque para falar” | Provider e layout estão implementados; adicionar e acionar pelo Launcher permanece **A validar**. |

## Delimitação do MVP
Priorizar comandos simples, feedback, validação, testes e uma demonstração visível da política de risco. **Não** tratar um fallback com LLM, mensagens reais, automação irrestrita ou leitura universal de toda a tela como requisitos de implementação automática.

## IA no projeto
O enunciado exige algum nível de uso de IA; o histórico considera IA como apoio à codificação/documentação e possível fallback de interpretação. A integração runtime ao aplicativo não está comprovada. Relatar uso real, revisão humana, erros e limites.

## Valor pretendido
Interface alternativa e mais acessível; execução controlada; previsibilidade; manutenção e auditabilidade por meio de arquitetura modular e testes.

## Referências internas
- `architecture.md` para desenho técnico.
- `../engineering/requirements.md` para funcionalidades e rastreabilidade.
- `../milestones/rt03-plan.md` para entrega acadêmica atual.
