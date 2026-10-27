# Princípios de engenharia de software

**Status:** princípios selecionados para orientar implementação; aderência real requer revisão de código.

## Clean Code
Nomes claros, funções coesas, poucos efeitos colaterais inesperados, responsabilidades explícitas, erros tratados de modo legível, sem duplicação evitável. Evitar camadas vazias criadas apenas para cumprir um padrão.

## SOLID aplicado ao Acesso60
| Princípio | Aplicação prática | Antipadrão |
|---|---|---|
| SRP | `CommandInterpreter` só interpreta; `RiskPolicy` só avalia risco | Interpretador manipula UI e permissões |
| OCP | Novos tipos de comando ampliam o domínio com baixo impacto | Alterar múltiplos módulos não relacionados |
| LSP | Fakes de `SpeechToTextEngine` respeitam o contrato real | Fake omite erros previstos |
| ISP | Interfaces pequenas para STT, executor e feedback | Interface gigante dependente de Android |
| DIP | Domínio usa abstração `CommandExecutor` | Dominío importa `AccessibilityService` |

## Princípios complementares
- **KISS:** priorizar pipeline compreensível e comportamentos limitados.
- **DRY:** centralizar regras de validação e risco; evitar abstração prematura.
- **YAGNI:** IA runtime, armazenamento persistente e comandos complexos somente com necessidade aprovada.
- **Fail-safe:** na dúvida, não executar ações sensíveis.
- **Least privilege:** apenas permissões Android essenciais.
- **Separation of concerns:** UI, STT, interpretação, política e execução independentes.
- **Testability:** interfaces injetáveis, valores tipados, lógica determinística.

## Exemplos de política de mudança
1. Mudou gramática de comando? Revisar `CommandInterpreter` e testes.
2. Mudou execução? Revisar executor e integração Android sem replicar validador.
3. Mudou risco? Atualizar `RiskPolicy`, casos de autorização e documentação.
4. Adicionou LLM? Resposta só produz intenção candidata, sempre validada como entrada não confiável.
