# Estratégia de testes — Acesso60

## Objetivos e Padrão-Ouro Normativo
Verificar o processamento de comandos, proteção contra execuções indevidas, feedback acessível e integração Android, em total alinhamento aos seis critérios acadêmicos e aos padrões internacionais:
- **ISO/IEC/IEEE 29119 / 29119-1:** Conceitos, processos e modelo de teste por camadas.
- **IEEE 829:** Estrutura documental de especificação, procedimentos e relatórios de evidências.
- **IEEE 1012:** Verificação e Validação (V&V) rigorosa, com nível máximo de integridade aplicado à política de risco.
- **IEEE 1028:** Auditorias e revisões sistemáticas de testes e integridade de código.
- **IEEE 1044:** Classificação e triagem padronizada de anomalias e falhas de software.
- **ISO 9000:2026:** Foco contínuo no usuário final (pessoas idosas) e controle de processos por evidências.

## Pirâmide inicial (ISO/IEC/IEEE 29119-1)
1. **Unitários:** interpretador, validador, política de risco e roteador, isolados do SDK Android (JVM pura via JUnit 5).
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

## Evidências a preservar (IEEE 829 e IEEE 1044)
Conforme estruturação do **IEEE 829** (relatório de teste) e classificação de anomalias do **IEEE 1044**:
Commit, data, dispositivo/Android, identificador, cenário, comando executado, resultado esperado, resultado observado, logs sanitizados, screenshot/vídeo quando necessário e status formal (`passou`, `falhou`, `bloqueado`, `não executado`). Em caso de anomalia, registrar severidade e impacto no usuário.

## Metas
- **Obrigatório por segurança:** nenhum teste deve comprovar execução de ação sensível sem autorização.
- Cobertura, tempo de resposta e taxa de comandos reconhecidos exigem coleta de baseline e definição de limiares antes de afirmar conformidade.
- Testes com usuários reais dependem de consentimento e desenho apropriado; não inventar resultados.

## Risco de testes com hardware
`SpeechRecognizer` depende da disponibilidade/configuração do dispositivo; `AccessibilityService` depende de ativação explícita. Nos testes unitários, utilizar fakes para isolar essas dependências. O resultado em mock não substitui teste E2E real.

## Execucao auditada em 28/10/2026

Foram executados com sucesso `:core:test`, `:app:testDebugUnitTest`,
`:app:lintDebug`, `:app:assembleDebug` e
`:app:connectedDebugAndroidTest` (5/5 no emulador API 37). A cobertura
modular do controlador real inclui comando desconhecido, erro de STT e
confirmacao de comando sensivel. Audio pre-gravado, STT real e execucao real
de `voltar` continuam **A validar**.
