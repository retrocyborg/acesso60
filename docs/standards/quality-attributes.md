# Atributos de qualidade e critérios de avaliação

**Distinção importante:** os seis critérios acadêmicos de comprovação QA estão em `qa-rules.md`. Aqui estão atributos de qualidade do **produto**, inspirados em modelos de qualidade como ISO/IEC 25010, com indicadores propostos, sem métricas inventadas.

| Atributo priorizado | Aplicação | Métrica proposta | Situação |
|---|---|---|---|
| Adequação funcional | Comando produz resultado correto | Comandos corretos / total de casos | Sem medição confirmada |
| Confiabilidade | Erros recuperáveis e estados previsíveis | Falhas por execução e taxa de recuperação | Sem medição confirmada |
| Usabilidade | Fluxos legíveis e fáceis para o público | Sucesso de tarefas; tentativas; feedback | Sem teste com usuários confirmado |
| Eficiência de desempenho | Voz → resposta em tempo adequado | Latência mediana e p95 em dispositivo-alvo | Sem limite formal confirmado |
| Segurança | Bloqueio de ações inseguras; privacidade | Ações sensíveis sem autorização = 0 (meta) | Sem teste confirmado |
| Manutenibilidade | Mudanças isoladas, testes e baixo acoplamento | Complexidade, linters e cobertura | Sem relatório confirmado |

## Métodos propostos
- Casos de uso escritos com comandos válidos, ambíguos e inválidos.
- Medir desde término da entrada de voz até feedback e registrar modelo/Android/rede.
- Verificar leituras por tecnologias assistivas, labels Compose e estados de escuta.
- Separar falhas de microfone, STT, interpretação, autorização e execução.
- Priorizar confiabilidade da negação segura a comandos incorretos.

## Observações
- Percentuais mínimos de cobertura, tempos de resposta e configurações de hardware **não foram fixados oficialmente neste conjunto de conversas**; definí-los como critério de aceite requer aprovação e benchmark.
- Normas de qualidade são referência conceitual; não afirmar certificação.
- Os seis critérios da disciplina não correspondem automaticamente a seis atributos da ISO/IEC 25010.
