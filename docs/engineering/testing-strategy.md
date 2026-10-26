# Estratégia de testes — Acesso60

## Objetivos
Verificar o processamento de comandos, proteção contra execuções indevidas, feedback acessível e integração Android, alinhados aos seis critérios de qualidade do trabalho.

## Pirâmide inicial
1. **Unitários:** interpretador, validador, política de risco e roteador, isolados do SDK Android.
2. **Integração:** percurso texto → comando → validação → risco → executor fake → feedback.
3. **UI:** Compose nos estados inicial, escutando, confirmação, erro e sucesso.
4. **E2E:** comandos em emulador/dispositivo, com permissões/serviço habilitados de maneira legítima.

## Casos mínimos propostos
| Teste | Entrada/condição | Esperado |
|---|---|---|
| INT-01 | `voltar` | `Command.Back` |
| INT-02 | `início` e `inicio` | `Command.Home` ou resultado equivalente |
| INT-03 | texto sem comando reconhecível | falha de interpretação sem execução |
| VAL-01 | comando nulo/incompleto | inválido |
| RISK-01 | `Back` | categoria baixa conforme política aprovada |
| RISK-02 | ação sensível simulada | requer confirmação ou bloqueio |
| ROUTE-01 | comando autorizado | roteia somente uma vez |
| E2E-01 | “voltar” com serviço ativo | tentativa de BACK e feedback honesto |
| E2E-02 | ação sensível cancelada | nenhuma ação realizada |
| E2E-03 | STT falha ou transcrição vazia | mensagem de recuperação |
| UI-01 | permissão do microfone negada | orientação acessível, sem crash |

## Evidências a preservar
Commit, data, dispositivo/Android, identificador, cenário, comando executado, esperado, observado, logs sanitizados, screenshot/vídeo quando necessário e status (passou/falhou/bloqueado/não executado).

## Metas
- **Obrigatório por segurança:** nenhum teste deve comprovar execução de ação sensível sem autorização.
- Cobertura, tempo de resposta e taxa de comandos reconhecidos exigem coleta de baseline e definição de limiares antes de afirmar conformidade.
- Testes com usuários reais dependem de consentimento e desenho apropriado; não inventar resultados.

## Risco de testes com hardware
`SpeechRecognizer` depende da disponibilidade/configuração do dispositivo; `AccessibilityService` depende de ativação explícita. Nos testes unitários, utilizar fakes para isolar essas dependências. O resultado em mock não substitui teste E2E real.
