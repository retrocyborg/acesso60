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

## Princípios de Gestão da Qualidade (ISO 9000:2026)

Em complemento aos atributos de produto (ISO/IEC 25010), o projeto adota os princípios de gestão da qualidade fundamentados na **ISO 9000:2026**:
1. **Foco no Usuário:** Desenho voltado prioritariamente às necessidades e limitações cognitivas/motoras da pessoa idosa.
2. **Abordagem por Processos:** Ciclo claro e rastreável de engenharia (especificação $\rightarrow$ implementação $\rightarrow$ testes $\rightarrow$ registro de evidências).
3. **Tomada de Decisão Baseada em Evidências:** Nenhum status ou conformidade é aceito sem comprovação por dados, execuções e logs reais.
4. **Melhoria Contínua:** Tratamento metódico de anomalias (IEEE 1044) e refinamento incremental do núcleo e da interface.
5. **Engajamento e Transparência:** Código limpo e revisado (IEEE 1028), documentação rastreável e governança explícita de IA.

## Observações
- Percentuais mínimos de cobertura, tempos de resposta e configurações de hardware **não foram fixados oficialmente neste conjunto de conversas**; definí-los como critério de aceite requer aprovação e benchmark.
- Normas de qualidade são referência conceitual; não afirmar certificação.
- Os seis critérios da disciplina não correspondem automaticamente a seis atributos da ISO/IEC 25010.
