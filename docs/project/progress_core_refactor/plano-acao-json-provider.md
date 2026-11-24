# Plano de aÃ§Ã£o â€” catÃ¡logo JSON e sincronizaÃ§Ã£o

**Estado:** P0 parcialmente implementado e validado em 24/11/2026; P1 ainda possui pendÃªncias.
**Base da anÃ¡lise:** auditoria de cÃ³digo de 24/11/2026 em `auditoria-json-provider.md`.
**Escopo:** `JsonAppDefinitionProvider`, `ReentrantLock`, gravaÃ§Ã£o de aliases e integraÃ§Ã£o com o registro usado para abrir aplicativos.

## Objetivo

Fazer com que o alias comunicado ao usuÃ¡rio seja consultÃ¡vel e persistido conforme o contrato definido, estabilizar o carregamento do catÃ¡logo e comprovar a seguranÃ§a concorrente do cache e do arquivo sem manter I/O dentro do `ReentrantLock`.

## Ordem de execuÃ§Ã£o

### AÃ§Ã£o 1 â€” Definir o contrato do catÃ¡logo

**Prioridade:** P0 â€” prÃ©-condiÃ§Ã£o para mudanÃ§as de implementaÃ§Ã£o.
**DecisÃ£o proposta:** arquivo local vÃ¡lido prevalece; se estiver ausente ou vazio, usar o asset; definir explicitamente se arquivo malformado causa fallback ao asset ou falha fechada.

**EntregÃ¡veis:** tabela de cenÃ¡rios e resultados; decisÃ£o registrada na documentaÃ§Ã£o de arquitetura ou junto ao provider.

**ValidaÃ§Ã£o:** testes para local vÃ¡lido, local ausente, vazio, malformado, asset vÃ¡lido e asset ausente.

### AÃ§Ã£o 2 â€” Unificar a fonte de dados usada pelos handlers

**Prioridade:** P0.
**MudanÃ§a proposta:** criar uma Ãºnica instÃ¢ncia/repositÃ³rio compartilhado para leitura e atualizaÃ§Ã£o. Fazer `OpenAppCommandHandler` consultar o estado atualizado, ou publicar uma atualizaÃ§Ã£o do registry apÃ³s a mutaÃ§Ã£o. NÃ£o manter uma cÃ³pia estÃ¡tica desatualizada.

**EntregÃ¡veis:** wiring com instÃ¢ncia compartilhada; teste que adiciona um alias e resolve esse alias pelo fluxo de abertura sem reiniciar o app.

**ValidaÃ§Ã£o:** teste de integraÃ§Ã£o do provider + registry + handlers; confirmar que aliases preexistentes continuam resolvendo e conflitos continuam rejeitados.

### AÃ§Ã£o 3 â€” Alinhar o resultado do comando com a persistÃªncia

**Prioridade:** P0.
**MudanÃ§a proposta:** definir se o resultado significa â€œalteraÃ§Ã£o aplicada ao cacheâ€, â€œgravaÃ§Ã£o agendadaâ€ ou â€œgravaÃ§Ã£o concluÃ­daâ€. A interface sÃ³ deve comunicar `Success` conforme o contrato escolhido; falhas de persistÃªncia precisam chegar ao resultado ou a um estado explÃ­cito de pendÃªncia.

**EntregÃ¡veis:** contrato tipado e fluxo de erro; teste de sucesso, falha de escrita e cancelamento.

**ValidaÃ§Ã£o:** simular armazenamento que falha e confirmar que o handler/UI nÃ£o informa persistÃªncia bem-sucedida sem evidÃªncia.

### AÃ§Ã£o 4 â€” Tornar testes isolados e determinÃ­sticos

**Prioridade:** P1, apÃ³s as decisÃµes P0.
**MudanÃ§a proposta:** fornecer asset/fixture explÃ­cito e isolamento do arquivo por teste; permitir controlar dispatcher/agendador do provider sem estado global mutÃ¡vel. Encerrar providers e scopes em `finally`/fixture.

**EntregÃ¡veis:** testes separados para fallback de asset e cache vazio; teste de debounce controlado pelo scheduler; concorrÃªncia que mede apenas entradas inseridas pelo cenÃ¡rio.

**ValidaÃ§Ã£o:** executar os trÃªs testes instrumentados que falharam; repetir cada teste isoladamente e em conjunto para detectar dependÃªncia de ordem.

### AÃ§Ã£o 5 â€” Verificar concorrÃªncia e decidir polÃ­tica de gravaÃ§Ã£o

**Prioridade:** P1.
**MudanÃ§a proposta:** manter `ReentrantLock` para leituras/mutaÃ§Ãµes sÃ­ncronas curtas do cache. NÃ£o suspender nem fazer I/O dentro dele. Usar um armazenamento fake com barreiras para testar se jobs jÃ¡ iniciados podem gravar snapshots fora de ordem.

**EntregÃ¡veis:** teste reprodutÃ­vel da intercalaÃ§Ã£o entre captura de snapshot, cancelamento e escrita; decisÃ£o documentada sobre serializaÃ§Ã£o/commit atÃ´mico.

**ValidaÃ§Ã£o:** comprovar que a Ãºltima atualizaÃ§Ã£o concluÃ­da permanece no arquivo. SÃ³ adicionar mutex de persistÃªncia, worker Ãºnico ou escrita atÃ´mica se o teste demonstrar necessidade.

### AÃ§Ã£o 6 â€” Fechar o ciclo de vida

**Prioridade:** P1.
**MudanÃ§a proposta:** usar `applicationContext` para armazenamento e definir um Ãºnico dono do provider/scope. Especificar comportamento apÃ³s `close()` e evitar scopes independentes sem ciclo de cancelamento.

**EntregÃ¡veis:** wiring com escopo de vida definido; testes de fechamento repetido, atualizaÃ§Ã£o apÃ³s fechamento e cancelamento durante debounce/gravaÃ§Ã£o.

**ValidaÃ§Ã£o:** verificar configuraÃ§Ã£o normal e recriaÃ§Ã£o da Activity; confirmar que trabalhos pendentes seguem a polÃ­tica definida e que o provider nÃ£o retÃ©m Activity.

### AÃ§Ã£o 7 â€” RegressÃ£o e teste do aplicativo

**Prioridade:** gate final.
**Procedimentos:**

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug :app:assembleDebug
```

**CritÃ©rio de avanÃ§o:** todos os testes focados e a suÃ­te instrumentada passam; lint e build concluem. Depois disso, instalar/abrir o APK no AVD e executar o fluxo de adicionar alias â†’ abrir pelo alias. Registrar dump/log sanitizado e resultado observado.

**Limite da evidÃªncia:** aprovaÃ§Ã£o dos testes instrumentados nÃ£o substitui validaÃ§Ã£o de fala real, leitor de tela ou uso fÃ­sico.

## Plano específico — confirmar persistência do alias

### Problema

`addAlias` altera a memória antes de a escrita em disco terminar. Se a gravação falhar ou o processo for encerrado nesse intervalo, o alias funciona na sessão atual e pode desaparecer após reiniciar. A mensagem “salvamento em andamento” evita afirmar persistência, mas não informa a falha tardia nem define se a alteração será desfeita.

### Decisão proposta para revisão

Separar três estados no contrato: `AppliedInMemory` (disponível nesta sessão), `Persisted` (gravação concluída) e `PersistenceFailed` (persistência não confirmada). O handler não deve reportar `Persisted` antes da confirmação do provider. Na falha, comunicar que o alias permanece apenas nesta sessão ou reverter a memória; a política deve ser única. Preferência inicial: manter em memória, exibir aviso explícito de não persistência e permitir nova tentativa, evitando remoção inesperada durante uso. Essa preferência é **A validar** com revisão do comportamento de produto.

### Telas e estados para cada caso

A proposta é usar a tela existente `ResultScreen` como base e apresentar conteúdo e ações específicos do alias. Não é necessário criar quatro rotas/telas independentes: são quatro estados visuais nomeados, com ações apropriadas, evitando duplicar componentes e mantendo navegação consistente.

1. **Salvando — `AppliedInMemory`:** informar “Alias disponível nesta sessão; salvando…”; deixar claro que ainda não está confirmado em disco. Não mostrar sucesso final. Aguardar resultado assíncrono sem bloquear a UI.
2. **Salvo — `Persisted`:** informar “Alias salvo e pronto para uso”; oferecer ação para concluir/voltar ao início. Só entrar neste estado depois da confirmação do snapshot gravado.
3. **Falha ao salvar — `PersistenceFailed`:** manter o alias disponível na sessão conforme decisão aprovada; informar que ele pode desaparecer ao fechar/reiniciar o app; apresentar ação **Tentar salvar novamente** e ação para concluir. Falha não deve remover o alias nem ser apresentada como sucesso.
4. **Cancelado/sem confirmação:** informar “Não foi possível confirmar o salvamento; o alias continua disponível nesta sessão até você fechar o app” apenas se a atualização em memória já ocorreu. Se cancelado antes dessa atualização, informar que nenhuma alteração foi aplicada. Evitar uma mensagem única que sugira rollback quando não houve rollback.

**Acessibilidade e feedback:** títulos curtos e específicos, mensagem legível em texto ampliado e anunciada por leitor de tela; cor/ícone não são o único indicador. O feedback falado deve refletir o mesmo estado visual, sem afirmar persistência antecipadamente.

**Decisões de navegação A validar:** se o resultado de gravação chegar depois de o usuário sair da tela, definir um aviso persistente/retorno de estado sem interromper comando em andamento; avaliar retenção do aviso de falha enquanto o alias continuar apenas em memória. O retry deve ser idempotente e não duplicar aliases.
### Etapas propostas

1. Substituir a semântica ambígua de `addAlias(): AppDefinition?` mais `schedulePersist()` por um contrato observável, sem bloquear a thread de UI. A confirmação deve corresponder à gravação do snapshot que contém o alias solicitado.
2. Preservar a distinção entre estado da sessão e estado persistido. Se debounce combinar alterações, cada solicitação pendente deve receber resultado coerente com o snapshot gravado ou com a falha correspondente.
3. Tratar cancelamento/fechamento como resultado não confirmado, sem converter cancelamento em sucesso. Definir como falha tardia será comunicada e como retry será acionado.
4. Apresentar na UI estados distintos de “disponível nesta sessão”, “salvo” e “não foi possível salvar”. Não expor stack trace ou detalhes internos; registrar diagnóstico técnico sanitizado.
5. Definir a garantia de gravação. Serializar commits se o teste reproduzir reordenação; avaliar gravação temporária seguida de substituição atômica se suportada e necessária. Não afirmar durabilidade contra encerramento abrupto sem evidência da estratégia.

### Critérios de validação propostos

- Sucesso: o alias resolve na sessão; “salvo” só é confirmado depois de reler do armazenamento um catálogo que contém o alias.
- Falha de escrita: handler/UI distinguem memória de disco e não declaram persistência; manter ou reverter segue a decisão aprovada.
- Cancelamento/fechamento antes do commit: resultado permanece não confirmado, sem falso sucesso.
- Debounce com múltiplas alterações: arquivo final contém todos os aliases aceitos e cada solicitação recebe conclusão coerente.
- Reabertura do provider/app: alias confirmado continua disponível; alias não confirmado segue a política comunicada.
- Regressão: aliases existentes e conflitos preservam o comportamento; evidências JVM, instrumentadas e manuais são registradas separadamente.

**Riscos e limites:** confirmação de escrita do sistema de arquivos não prova, por si só, durabilidade diante de falha física; aguardar persistência não pode bloquear a UI; manter o alias em memória após falha cria divergência temporária e requer retry/mensagem clara.

**Status:** política aprovada pelo usuário e implementada nesta sessão. Os estados do provider e da UI foram verificados por testes; durabilidade em interrupção abrupta e fluxo manual permanecem A validar.
## Estado da execuÃ§Ã£o â€” 24/11/2026

| AÃ§Ã£o | SituaÃ§Ã£o observada | EvidÃªncia / pendÃªncia |
|---|---|---|
| 1 â€” Contrato do catÃ¡logo | Parcialmente implementada | Local vÃ¡lido prevalece; ausÃªncia, vazio e JSON malformado usam asset. Malformado e asset ausente testados; local vÃ¡lido/vazio ainda sem caso dedicado. |
| 2 â€” Fonte compartilhada | Implementada e verificada | Uma instÃ¢ncia em `MainActivity`; `ProviderAppRegistry` consulta dados atuais; testes JVM cobrem alias visÃ­vel apÃ³s atualizaÃ§Ã£o. Fluxo vocal manual pendente. |
| 3 â€” Resultado e persistÃªncia | Parcialmente implementada | Cache atualiza de modo atÃ´mico; mensagem explicita â€œsalvamento em andamentoâ€; UI marca `AddAlias` como nÃ£o verificado. Falha/cancelamento tardio ainda nÃ£o Ã© comunicado Ã  UI. |
| 4 â€” Testes determinÃ­sticos | Implementada e verificada | Dispatcher injetÃ¡vel; suÃ­te `:app:connectedDebugAndroidTest` passou com 10/10. Testes nÃ£o foram repetidos isoladamente. |
| 5 â€” Corridas de gravaÃ§Ã£o | Pendente | ReordenaÃ§Ã£o de snapshots ainda Ã© hipÃ³tese sem reproduÃ§Ã£o. |
| 6 â€” Ciclo de vida | Parcial | `applicationContext` usado; owner do scope e chamada a `close()` ainda pendentes. |
| 7 â€” RegressÃ£o e app | Parcialmente verificada | JVM, lint, build passaram; 10/10 instrumentados; APK instalado e `MainActivity` observada no AVD. Fluxo real de fala adicionarâ†’abrir alias nÃ£o demonstrado. |

Comando final de JVM/lint/build: `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` â€” `BUILD SUCCESSFUL`; app JVM 73/73 sem falhas. Instrumentados: `:app:connectedDebugAndroidTest` â€” 10/10 no `Medium_Phone_API_37.0`.

## Matriz de rastreabilidade

| Achado | AÃ§Ãµes | SituaÃ§Ã£o |
|---|---|---|
| AUD-JP-01 â€” registry nÃ£o atualizado | 2, 7 | Corrigido; alias fica visÃ­vel pelo registry dinÃ¢mico na sessÃ£o |
| AUD-JP-02 â€” sucesso antes da persistÃªncia | 3, 7 | Parcial; resposta explicita salvamento em andamento e UI mantÃ©m estado nÃ£o verificado; falha tardia nÃ£o aparece |
| AUD-JP-03 â€” teste de cache vazio contradiz fallback | 1, 4 | Corrigido; cenÃ¡rio usa asset ausente explicitamente |
| AUD-JP-04 â€” scheduler nÃ£o controla `Dispatchers.IO` | 4 | Corrigido; dispatcher injetÃ¡vel e instrumentados passaram |
| AUD-JP-05 â€” JSON malformado nÃ£o usa fallback | 1 | Corrigido; fallback coberto por teste instrumentado |
| AUD-JP-06 â€” uso atual do lock | 5 | Sem violaÃ§Ã£o observada; manter atÃ© evidÃªncia nova |
| AUD-JP-07 â€” gravaÃ§Ã£o fora de ordem | 5 | HipÃ³tese; reproduzir antes de corrigir |
| AUD-JP-08 â€” escopo/contexto sem fechamento | 6 | Ciclo nÃ£o localizado; impacto em runtime a validar |

## Registro por aÃ§Ã£o

Ao executar cada aÃ§Ã£o, registrar data, branch/commit, arquivos, comando/procedimento, resultado esperado, resultado observado, status, evidÃªncia e limitaÃ§Ãµes. Atualizar este plano e o histÃ³rico de desenvolvimento somente com resultados reproduzÃ­veis.
