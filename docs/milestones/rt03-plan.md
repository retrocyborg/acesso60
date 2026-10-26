# RT03 — plano de consolidação e qualidade

**Prazo no enunciado:** 28/10/2026. **Registro:** GitBook e GitHub.
**Fonte:** enunciado da UC Qualidade de Software disponibilizado na conversa, seção RT03.

## Escopo obrigatório do enunciado
1. Documentação consolidada (escopo, requisitos, arquitetura e protótipo final).
2. Protótipo interativo de alta fidelidade **ou** versão alfa/beta demonstrável (conforme opção prevista).
3. Relatório de uso de IA: ferramentas, código/regras gerados, validação humana, riscos e mitigação.
4. Evidências iniciais de QA: validação de entradas e tratamento de erros sem stack trace bruto na UI.
5. Suíte inicial de testes unitários basais, com execução demonstrável.
6. Planejamento e roteiro em slides para a banca final.

## Plano técnico recomendado (não confundir com exigência literal)
| Prioridade | Pacote | Saída demonstrável |
|---|---|---|
| P0 | Revisão do repositório | Inventário real de arquivos, build e pendências |
| P0 | Núcleo de comandos | Interpretação, validação e risco com testes |
| P0 | Erros e feedback | Demonstração de comando desconhecido/erro tratado |
| P0 | Protótipo ou alfa/beta | Telas navegáveis ou fluxo Android mínimo |
| P0 | Relatório de IA | Casos concretos e revisão humana |
| P0 | Consolidação GitBook | Páginas e links atualizados |
| P1 | Fluxo HOME/BACK no Android | E2E em dispositivo se viável |
| P1 | Análise estática/CI | Logs de execução verificáveis |
| P1 | Rastreabilidade | RF → código → teste → evidência |

## Definição de pronto para cada item
- Artefato revisado e acessível.
- Critério de aceite descrito e resultado observado registrado.
- Evidência datada, não simplesmente uma afirmação.
- Limitações e itens não implementados declarados.

## RT04 não deve ser presumida concluída
O enunciado situa comprovação final dos seis critérios, métricas finais, relatório final de testes, documentação completa e APK publicado na RT04 (18/11/2026).

## Roteiro de apresentação RT03 proposto
Problema → solução → arquitetura → telas → fluxo de validação/erro → teste unitário real → uso crítico de IA → limitações → próximos passos RT04.

## Próximos passos imediatos
Auditar GitHub; eleger opção de protótipo/alfa-beta; consolidar documentos existentes; executar e registrar ao menos testes básicos; produzir relatório de IA com ocorrências reais.
