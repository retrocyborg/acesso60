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

## Regra obrigatÃ³ria de arquitetura compartilhada

O domÃ­nio de comandos deve possuir uma Ãºnica implementaÃ§Ã£o em
`acesso60-core`, incluindo modelos, interpretaÃ§Ã£o, validaÃ§Ã£o, polÃ­tica de
risco e resultados. O projeto Android deve declarar o nÃºcleo como dependÃªncia
e fornecer apenas adaptadores para UI, STT, permissÃµes, roteamento e APIs do
sistema.

Ã‰ proibido duplicar no Android classes ou regras equivalentes a
`Command`, `CommandInterpreter`, `CommandValidator` e `RiskPolicy`. Se uma
diferenÃ§a for necessÃ¡ria, ela deve ser um adaptador explÃ­cito, ter contrato
documentado e possuir teste que comprove a equivalÃªncia esperada.

**ValidaÃ§Ã£o obrigatÃ³ria:** toda alteraÃ§Ã£o no contrato compartilhado deve
passar pelos testes do nÃºcleo; toda integraÃ§Ã£o Android deve acrescentar teste
de contrato no mÃ³dulo Android quando o SDK estiver disponÃ­vel. A existÃªncia da
dependÃªncia Gradle nÃ£o constitui, sozinha, prova de integraÃ§Ã£o funcional.

## Contratos de adaptadores

- `RecognizerClient` isola `SpeechRecognizer`; `AndroidRecognizerClient` é o
  adaptador Android e `AndroidSpeechToTextEngine` normaliza callbacks.
- `AccessibilityGateway` isola o acesso ao serviço; `AndroidAccessibilityGateway`
  conecta o executor ao `Acesso60AccessibilityService`.
- `AndroidCommandExecutor` pode ser testado com gateway fake, sem serviço ativo.
- A conexão real do `AccessibilityService` continua `A validar` por teste
  instrumentado/manual.

## Testabilidade
Injetar interfaces para STT, executor e feedback. Testar domínio fora do Android; testar integração real em emulador/dispositivo quando possível.

## Extensibilidade por camadas e plugins — decisão proposta

Para a próxima etapa, propõe-se evoluir o conjunto fechado de comandos para
uma arquitetura extensível, mantendo as seguintes camadas e limites:

1. **Interpretação:** identifica intenção e parâmetros por aliases ou parsers
   registrados.
2. **Validação:** rejeita parâmetros ausentes, inválidos ou ambíguos.
3. **Risco e autorização:** classifica o comando e exige confirmação quando
   aplicável.
4. **Roteamento:** localiza o handler registrado somente após autorização.
5. **Execução:** adapta o comando às APIs Android e retorna resultado tipado.
6. **Feedback:** comunica sucesso, indisponibilidade ou falha sem presumir que
   uma ação ocorreu.

O mecanismo proposto deverá usar contratos equivalentes a
`CommandDefinition`, `CommandRegistry` e `CommandHandler`. Um plugin poderá
declarar aliases, parser de parâmetros e handler, mas não poderá substituir a
validação, a política de risco ou a autorização central. Plugins externos ou
carregamento dinâmico permanecem **A validar** quanto a assinatura, origem,
compatibilidade, isolamento, permissões e regras de distribuição Android.

Esta seção registra direção arquitetural, não implementação verificada. A
adoção dependerá de testes de contrato, falha segura, compatibilidade de versão
e comprovação de que um plugin defeituoso não executa ações não autorizadas.
