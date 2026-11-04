# Relatório de uso de IA — RT03 (rascunho)

**Estado:** rascunho para revisão humana.
**Atualizado:** 04/11/2026.
**Escopo:** registros localizados no repositório e atividades desta sessão; não representa inventário integral de todas as interações da equipe.

## Finalidade e ferramentas

O histórico do projeto registra uso de IA como apoio à codificação e à
documentação, incluindo propostas de aliases em português e casos de teste.
Nesta sessão, o Codex (modelo GPT-6) foi usado para ler documentação/código,
comparar o estado documental com alterações locais e redigir atualizações de
rastreabilidade, roadmap, status, histórico e este rascunho. O nome/versionamento
das ferramentas usadas nas etapas históricas, prompts completos e autoria de cada linha de código
não podem ser reconstruídos integralmente pelos arquivos consultados: **A validar**.

## Saídas assistidas e estado verificável

| Saída | Participação de IA registrada ou observada | Revisão/validação disponível | Limite |
|---|---|---|---|
| Código Android e testes | O repositório/histórico registra assistência de IA durante o desenvolvimento; atribuição por arquivo ou linha não está disponível | Nesta revisão, `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug` e `:app:assembleDebug` passaram; resultados são registrados em `rt03-status.md` | Atribuição detalhada e revisão humana formal por mudança: **A validar** |
| Aliases de voz e testes | A decisão de 03/11 propõe IA de desenvolvimento para sugerir variações; execução do app permanece determinística | A proposta exige revisão humana, testes de colisão e proteção contra ambiguidade | Não há inventário de sugestões aceitas/rejeitadas: **A validar** |
| Documentação | Nesta sessão, a IA elaborou atualizações com base em fontes do próprio repositório e preservou estados **A validar** | Comparação posterior do diff e leitura dos arquivos alterados ainda deve ser feita pelo responsável humano | Texto é rascunho; não foi confrontado com enunciado integral nem matriz oficial RT02 |

## Revisão humana e salvaguardas

- O responsável pelo projeto deve conferir cada afirmação e aprovar o texto
  antes de tratá-lo como relatório final ou submetê-lo academicamente.
- IDs `A60-RF-*` são provisórios enquanto a matriz oficial da RT02 não estiver
  disponível.
- Código gerado ou sugerido por IA deve permanecer sujeito a revisão, testes
  reproduzíveis e validação Android; existência no repositório não prova
  funcionamento.
- A IA não autoriza comandos nem substitui validação, política de risco ou
  confirmação do usuário no produto.
- Não foram incluídos dados pessoais, credenciais ou segredos neste rascunho.

## Riscos e limitações

1. **Erro factual ou documentação desatualizada:** mitigar com comparação com o
   código, logs de execução e documentos oficiais.
2. **Atribuição incompleta:** os arquivos consultados não mantêm proveniência
   por linha nem prompts completos; registrar isso como lacuna, sem inferir
   autoria.
3. **Falsa impressão de validação:** resultados históricos são citados com
   datas e escopo; as validações que faltam continuam marcadas **A validar**.
4. **Viés de automação:** manter decisão final e revisão das saídas com a equipe.

## Evidências e pendências para a versão final

- Evidências internas: `docs/project/development-history.md`,
  `docs/engineering/traceability.md`, `docs/engineering/testing-strategy.md` e
  `docs/milestones/rt03-status.md`.
- **A validar:** inventário das ferramentas/modelos efetivamente usados pela
  equipe, exemplos representativos de prompts/saídas, mudanças aceitas e
  rejeitadas, responsáveis pela revisão humana e correções decorrentes.
- **A validar:** conferir os itens acima com o enunciado oficial RT03 e anexar
  referências/evidências exigidas pela instituição.

## Critério para concluir o relatório

O relatório só deve ser considerado final após revisão da equipe, inclusão do
histórico integral que puder ser comprovado, conferência com o enunciado e
aprovação do responsável acadêmico. Este documento ainda não atende sozinho a
esse critério.
