# Arquitetura de referência — Acesso60

**Status:** desenho acordado nas conversas, não validado integralmente contra repositório.

## Pipeline principal
```text
Usuário → SpeechRecognizer → Texto → CommandInterpreter
      → CommandValidator → RiskPolicy → [Confirmação, se necessária]
      → CommandRouter → AndroidExecutor / AccessibilityService
      → Verificação (quando possível) → Feedback visual e/ou TTS
```

## Separação de módulos
| Módulo | Entradas | Saídas | Não deve fazer |
|---|---|---|---|
| SpeechToTextEngine | Áudio/estado de permissão | Texto/erro/estado | Executar ação Android |
| CommandInterpreter | Texto normalizado | `Command` ou falha | Conceder autorização |
| CommandValidator | `Command` + parâmetros | Válido/erro | Executar ou abrir telas |
| RiskPolicy | Comando + contexto | Risco/necessidade de confirmação | Modificar dados do usuário |
| CommandRouter | Comando autorizado | Plano de execução | Contornar política |
| AndroidExecutor | Plano autorizado | Resultado estruturado | Interpretar fala |
| AccessibilityService | Pedido explícito e autorizado | Ação/estado acessível | Ser mecanismo irrestrito de controle |
| Feedback | Resultado/erro | Informação ao usuário | Declarar êxito não verificado |

## Contratos conceituais
- `Command`: `Back`, `Home`, `OpenApp(appName)`, `ReadScreen`, `VolumeUp`, `VolumeDown`; outras ações apenas após definição de requisitos.
- `CommandInterpreter.interpret(text)`: produz comando explícito ou falha previsível.
- `RiskPolicy.evaluate(command, context)`: informa risco e autorização necessária.
- `CommandExecutor.execute(authorizedCommand)`: resultado tipado (`Success`, `Unavailable`, `Denied`, `Failure`) em vez de presumir sucesso.
- A resposta de IA, caso incluída, é **candidata de intenção**, jamais autorização ou executor.

## Política de risco proposta
- Navegação básica: risco usualmente baixo.
- Leitura de conteúdo e compartilhamento: dependem do contexto, privacidade e permissões.
- Envio de mensagens, exclusões e ações irreversíveis: confirmação/autorização apropriada e, se necessário, bloqueio.
- Desconhecimento de intenção, destinatário ou estado da permissão: não executar.

## Requisitos Android
- Tratar permissão `RECORD_AUDIO` e disponibilidade de serviço de reconhecimento.
- `AccessibilityService` requer declaração específica e ativação pelo usuário nas configurações do sistema.
- Não assumir que todo texto da tela está disponível pela árvore de acessibilidade.
- Verificar restrições da plataforma e regras de distribuição antes de uso em produção.

## Estrutura de pacotes sugerida
```text
app/src/main/java/com/acesso60/
├── ui/                  # Compose, estados e feedback
├── voice/               # SpeechRecognizer e TTS
├── command/             # modelos, interpretação e validação
├── security/            # política de risco e autorização
├── routing/             # roteamento de comandos
├── execution/           # integração Android
└── accessibility/       # serviço e adaptadores
```
Essa estrutura é proposta, não uma listagem confirmada do repositório.

## Fluxo de falhas
Permissão recusada, transcrição vazia, comando desconhecido, parâmetros incompletos, execução não suportada e cancelamento devem produzir resultados tipados e feedback acessível sem stack traces ao usuário.

## Testabilidade
Injetar interfaces para STT, executor e feedback. Testar domínio fora do Android; testar integração real em emulador/dispositivo quando possível.
