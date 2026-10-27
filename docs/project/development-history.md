# Histórico do desenvolvimento — Acesso60

**Natureza:** reconstrução das decisões registradas nas conversas, até 26/10/2026. **Não** substitui logs Git, tickets nem evidências do repositório.

## RT01 — concepção
- Definição de assistente Android de acessibilidade por voz para pessoas idosas.
- Discussão sobre problema, público-alvo, justificativa de mobile e atributos de qualidade.
- Escopo, bibliografia e Lean Canvas/Product Vision Board como artefatos acadêmicos.

## RT02 — requisitos e desenho
- Planejada documentação de RF/RNF, histórias de usuário, critérios de aceite e matriz de rastreabilidade.
- Desenhado pipeline de reconhecimento, interpretação, validação, risco, roteamento, execução e feedback.
- Propostas telas de central de assistência, ouvindo, confirmação, resultado e configurações.
- Selecionadas ferramentas de QA, prototipação, versionamento e testes.

## Esboço do projeto Android
Discussões anteriores citaram um esqueleto com `MainActivity.kt`, `AssistanceScreen.kt`, `SpeechToTextEngine.kt`, `Command.kt`, `CommandInterpreter.kt`, `CommandValidator.kt`, `RiskPolicy.kt`, `Acesso60AccessibilityService.kt`, Gradle e manifesto. O esqueleto representava **ponto de partida**, não aplicativo concluído. Um pedido anterior de envio como ZIP não gerou arquivo confirmado naquela tentativa.

## Integração GitHub
Repositório apontado: `https://github.com/retrocyborg/Acesso` (anteriormente foi considerado `retrocyborg/acesso60`). Tentativas de gravar arquivos por integração tiveram erro de permissão `403 Resource not accessible by integration`. **Não há confirmação aqui de publicação bem-sucedida de código-fonte.**

## RT03 — preparação
- Esboço de MVP, testes, Quality Gate, métricas e entregáveis.
- Definidas regras para avaliação de risco e confirmação de ações sensíveis.
- Separação entre exigências do enunciado, recomendações de engenharia e status verificável.
- Preparação de documentação Markdown modular para Codex, incluindo `AGENTS.md`.

## Não atribuir como fatos comprovados
- APK gerado/instalado.
- `SpeechRecognizer` funcional em dispositivo.
- Serviço de acessibilidade corretamente ativado.
- Testes executados e passando.
- Pipeline GitHub Actions configurado.
- Documentação integral publicada no GitBook.

## Próxima ação operacional
Inspecionar o repositório real, listar arquivos e commits, verificar build/testes e preencher `../milestones/rt03-status.md` com evidências concretas.
