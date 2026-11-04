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

## Regra obrigatória de arquitetura compartilhada

O domínio de comandos deve possuir uma única implementação em
`acesso60-core`, incluindo modelos, interpretação, validação, política de
risco e resultados. O projeto Android deve declarar o núcleo como dependência
e fornecer apenas adaptadores para UI, STT, permissões, roteamento e APIs do
sistema.

É proibido duplicar no Android classes ou regras equivalentes a
`Command`, `CommandInterpreter`, `CommandValidator` e `RiskPolicy`. Se uma
diferença for necessária, ela deve ser um adaptador explícito, ter contrato
documentado e possuir teste que comprove a equivalência esperada.

**Validação obrigatória:** toda alteração no contrato compartilhado deve
passar pelos testes do núcleo; toda integração Android deve acrescentar teste
de contrato no módulo Android quando o SDK estiver disponível. A existência da
dependência Gradle não constitui, sozinha, prova de integração funcional.

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

## Uso de IA para gerar aliases — decisão proposta

Por enquanto, a IA será usada durante o desenvolvimento para sugerir aliases,
variações linguísticas e casos de teste dos comandos. Após revisão humana,
esses dados serão incorporados ao interpretador determinístico e versionados
no projeto.

O aplicativo não deverá chamar uma IA em tempo de execução para classificar
comandos nesta etapa. O fluxo previsto é:

```text
IA no desenvolvimento → revisão humana → aliases versionados
  → STT → CommandInterpreter determinístico
  → CommandValidator → RiskPolicy → ExecutionWrapper
```

Essa decisão reduz latência, custo recorrente, dependência de rede e consumo
de memória no dispositivo. Ela não substitui o reconhecimento de fala: o STT
ainda precisa converter áudio em texto. A cobertura dos aliases, a qualidade
do STT em `pt-BR` e os requisitos mínimos de hardware permanecem **A validar**.

As sugestões da IA não devem ser incorporadas automaticamente. Cada alias
precisa ser revisado, testado contra colisões e associado a um comando permitido.
Frases ambíguas ou não cobertas devem resultar em `Unknown`, sem tentativa de
execução. Uma futura classificação por IA em tempo real poderá ser avaliada
como fallback, mas continuará subordinada à validação, à política de risco e à
autorização central.
## Wrapper de execução — arquitetura proposta

A execução deverá ter um ponto único de entrada no aplicativo Android. O
`CommandRouter` encaminhará somente comandos validados e autorizados para o
`ExecutionWrapper`. O wrapper coordenará a execução, mas não interpretará texto
nem decidirá a política de risco.

```text
CommandRouter
  → ExecutionWrapper
      → ExecutionMiddleware (opcional)
          → ExecutionRegistry
              → BackCommandExecutor
              → HomeCommandExecutor
              → VolumeCommandExecutor
              → OpenAppCommandExecutor
              → ReadScreenCommandExecutor
```

Contratos conceituais:

```kotlin
interface SpecializedCommandExecutor {
    fun supports(command: Command): Boolean
    fun execute(command: Command): ExecutionResult
}

interface ExecutionWrapper {
    fun execute(command: Command): ExecutionResult
}
```

O wrapper deverá localizar um único executor compatível, executar a operação e
devolver um `ExecutionResult` tipado. Ausência de executor, permissão
indisponível, serviço desconectado, parâmetro não suportado e exceção de API
Android devem produzir `Unavailable` ou `Failure` controlado, sem stack trace
exposto na interface.

O registry deverá rejeitar duas implementações para o mesmo comando, preservar
resolução determinística e permitir inspeção dos comandos suportados. Cada
executor especializado deverá depender de um gateway Android testável, como
`AccessibilityGateway`, em vez de acessar APIs globais diretamente quando isso
puder ser isolado.

Middleware de logging, métricas, correlação, timeout ou prevenção de execução
duplicada poderá envolver o registry. Esses componentes não podem autorizar
comandos, alterar a política de risco ou transformar uma falha em sucesso.

### Invariantes

1. O wrapper não recebe texto livre; recebe somente `Command` tipado.
2. A política de risco permanece no `CommandRouter` e no núcleo compartilhado.
3. Nenhum executor especializado executa comandos que não declara suportar.
4. Resultado de execução nunca é presumido como sucesso.
5. Falhas devem ser observáveis para logs e compreensíveis para o usuário.

### Sequenciamento de implementação

Esta arquitetura é uma decisão proposta. A implementação deverá começar
envolvendo o executor atual de `Back`, adicionar testes de contrato para
registry e wrapper e somente depois extrair executores de `Home`, volume e
abertura de aplicativos. A cobertura, o comportamento em dispositivo e a
integração completa com o `AccessibilityService` permanecem **A validar**.
