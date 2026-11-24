# Auditoria de cÃ³digo â€” JsonAppDefinitionProvider e ReentrantLock

**Data:** 24/11/2026
**Escopo:** provider JSON do catÃ¡logo de aplicativos, sincronizaÃ§Ã£o do cache e testes instrumentados relacionados.
**MÃ©todo:** inspeÃ§Ã£o estÃ¡tica dos consumidores, implementaÃ§Ã£o e testes; comparaÃ§Ã£o com as trÃªs falhas observadas em `:app:connectedDebugAndroidTest`. NÃ£o foram feitas alteraÃ§Ãµes no cÃ³digo nesta auditoria.

## Resumo executivo

O `ReentrantLock` protege adequadamente as operaÃ§Ãµes sÃ­ncronas observadas sobre o cache: nÃ£o hÃ¡ suspensÃ£o nem I/O de arquivo dentro de `withLock`. As falhas instrumentadas observadas decorrem principalmente de expectativas de teste incompatÃ­veis com o fallback documentado e do uso de `Dispatchers.IO` nÃ£o controlado pelo scheduler virtual.

A auditoria tambÃ©m encontrou um defeito funcional de integraÃ§Ã£o: a Activity cria duas instÃ¢ncias do provider e o registry de abertura Ã© uma cÃ³pia estÃ¡tica dos dados carregados. O handler de alias atualiza outra instÃ¢ncia; portanto, a nova definiÃ§Ã£o nÃ£o fica disponÃ­vel para resoluÃ§Ã£o na sessÃ£o corrente. O handler tambÃ©m comunica sucesso antes da gravaÃ§Ã£o persistente terminar.

## Achados

### AUD-JP-01 â€” Alias adicionado nÃ£o atualiza o registry ativo

**Impacto:** alto para a funÃ§Ã£o de adicionar alias.
**EvidÃªncia:** `MainActivity` chama `JsonAppDefinitionProvider.create(...)` separadamente para `OpenAppCommandHandler` e `AddAliasCommandHandler`. O primeiro provider Ã© convertido por `StaticAppRegistry(provider)` em uma lista/mapa carregados na construÃ§Ã£o. O segundo provider Ã© passado ao handler mutÃ¡vel. `StaticAppRegistry.resolve()` consulta apenas seu mapa prÃ©-calculado.

**ConclusÃ£o confirmada por inspeÃ§Ã£o:** alteraÃ§Ãµes feitas pela instÃ¢ncia mutÃ¡vel nÃ£o atualizam o mapa do registry jÃ¡ criado. A persistÃªncia poderia tornar o alias disponÃ­vel apÃ³s reconstruÃ§Ã£o/reinÃ­cio, mas isso nÃ£o foi validado nesta auditoria.

**AÃ§Ã£o proposta:** compartilhar uma Ãºnica fonte de dados e fazer o registry consultar estado atualizado, ou reconstruir/publicar o registry apÃ³s atualizaÃ§Ã£o. Adicionar teste de integraÃ§Ã£o: adicionar alias, entÃ£o resolver esse alias atravÃ©s do mesmo fluxo de abertura, sem reiniciar o app.

### AUD-JP-02 â€” Handler declara sucesso antes de persistir

**Impacto:** alto para a confiabilidade do feedback.
**EvidÃªncia:** `AddAliasCommandHandler.handle()` inicia `scope.launch { provider.addOrUpdate(merged) }` e retorna `ExecutionResult.Success` imediatamente. `addOrUpdate()` atualiza o cache e agenda debounce; nÃ£o espera a gravaÃ§Ã£o. `persistNow()` captura exceÃ§Ãµes de escrita e apenas registra log.

**ConclusÃ£o confirmada por inspeÃ§Ã£o:** `Success` significa que o trabalho foi agendado, nÃ£o que a persistÃªncia terminou. Falha ou cancelamento posterior nÃ£o altera o resultado jÃ¡ enviado Ã  UI.

**AÃ§Ã£o proposta:** definir resultado tipado para â€œaceito/agendadoâ€ versus â€œpersistidoâ€, ou tornar a operaÃ§Ã£o suspensa atÃ© o commit da gravaÃ§Ã£o e propagar falhas controladas. Testar erro de escrita, cancelamento e feedback resultante.

### AUD-JP-03 â€” Teste de cache vazio contradiz fallback de asset

**Impacto:** moderado para a confianÃ§a da suÃ­te.
**EvidÃªncia:** `loadFromStorage()` carrega o asset se o arquivo interno estiver ausente ou em branco. O asset versionado contÃ©m a definiÃ§Ã£o WhatsApp. `providerLoadsEmptyCacheWhenNoFileExists` apaga o arquivo local e espera cache vazio.

**ConclusÃ£o confirmada:** cenÃ¡rio e expectativa do teste nÃ£o correspondem ao contrato documentado/implementado. O teste de concorrÃªncia usa a mesma criaÃ§Ã£o padrÃ£o; por isso a entrada do asset explica a contagem observada de 6 em vez de 5.

**AÃ§Ã£o proposta:** manter teste explÃ­cito do fallback usando asset conhecido; testar cache vazio com asset de fixture vazio/inexistente. NÃ£o apagar o arquivo local como substituto de um fixture controlado.

### AUD-JP-04 â€” Scheduler do teste nÃ£o controla o debounce do provider

**Impacto:** moderado para determinismo dos testes.
**EvidÃªncia:** testes usam `runTest`/`advanceTimeBy`, enquanto o provider cria `CoroutineScope(SupervisorJob() + Dispatchers.IO)` prÃ³prio. A persistÃªncia usa `delay()` nesse dispatcher independente.

**ConclusÃ£o confirmada:** `advanceTimeBy` nÃ£o avanÃ§a o relÃ³gio usado pelo scope interno. A checagem do arquivo pode ocorrer antes da gravaÃ§Ã£o real; isto explica a falha observada do teste de debounce.

**AÃ§Ã£o proposta:** injetar dispatcher/escopo ou uma abstraÃ§Ã£o de agendamento controlÃ¡vel, mantendo configuraÃ§Ã£o de produÃ§Ã£o em `Dispatchers.IO`; aguardar condiÃ§Ã£o observÃ¡vel nos testes, sem sleeps arbitrÃ¡rios.

### AUD-JP-05 â€” Erro de JSON local nÃ£o usa fallback para asset

**Impacto:** moderado, comportamento degradado apÃ³s corrupÃ§Ã£o do arquivo.
**EvidÃªncia:** arquivo local em branco segue para asset, mas JSON local nÃ£o vazio e invÃ¡lido retorna `emptyList()` no catch interno, sem tentar asset.

**ConclusÃ£o confirmada por inspeÃ§Ã£o:** a precedÃªncia efetiva difere entre conteÃºdo em branco e conteÃºdo malformado. Se o asset Ã© a configuraÃ§Ã£o inicial/allowlist, corrupÃ§Ã£o do arquivo local remove essas entradas da sessÃ£o.

**AÃ§Ã£o proposta:** decidir formalmente se erro de parse local deve falhar fechado com lista vazia ou usar fallback do asset; codificar a decisÃ£o em teste e mensagem/log sem expor dados sensÃ­veis.

### AUD-JP-06 â€” ReentrantLock e seÃ§Ãµes crÃ­ticas

**Impacto:** sem defeito confirmado na disciplina atual do lock.
**EvidÃªncia:** `apps`, `provide()`, mutaÃ§Ã£o do cache, captura de snapshot, alteraÃ§Ã£o de `persistJob` e `close()` usam `withLock`. NÃ£o foi observado bloco suspensivo ou leitura/escrita de arquivo dentro de `withLock`.

**ConclusÃ£o:** os trechos observados sÃ£o sÃ­ncronos e curtos; `provide()` tambÃ©m Ã© sÃ­ncrono, entÃ£o substituir automaticamente por `Mutex` exigiria mudar o contrato. A operaÃ§Ã£o de cache e o agendamento sÃ£o duas seÃ§Ãµes crÃ­ticas separadas, mas os jobs sempre capturam o cache atual ao persistir.

**AÃ§Ã£o proposta:** manter o lock para estado sÃ­ncrono, documentar seus invariantes e adicionar teste concorrente de cache. NÃ£o trocar o mecanismo sem evidÃªncia de necessidade.

### AUD-JP-07 â€” GravaÃ§Ãµes possivelmente concorrentes ou fora de ordem

**Impacto:** potencial perda de atualizaÃ§Ã£o; nÃ£o reproduzido.
**EvidÃªncia:** cada job pode passar o atraso e entrar em `persistNow()`. O snapshot Ã© capturado sob lock, mas `file.writeText()` acontece fora dele; cancelamento de um job nÃ£o interrompe necessariamente uma escrita bloqueante jÃ¡ iniciada.

**ConclusÃ£o:** Ã© um risco plausÃ­vel, nÃ£o um defeito confirmado pelos testes atuais. NÃ£o hÃ¡ teste que force a intercalaÃ§Ã£o entre snapshot e escrita.

**AÃ§Ã£o proposta:** usar barreiras/fake de armazenamento para forÃ§ar a ordem adversa; apenas se reproduzir, serializar o commit do arquivo (por exemplo, worker Ãºnico ou mutex suspensivo separado) e testar substituiÃ§Ã£o atÃ´mica. NÃ£o manter `ReentrantLock` durante I/O.

### AUD-JP-08 â€” Contexto e escopo de corrotinas sem fechamento observado

**Impacto:** potencial retenÃ§Ã£o de Activity/recursos; ciclo de vida precisa validaÃ§Ã£o.
**EvidÃªncia:** provider guarda `Context`, cria scope prÃ³prio e oferece `close()`. `MainActivity` cria providers com `this@MainActivity`; a busca de referÃªncias nÃ£o encontrou chamadas a `close()`. A persistÃªncia do alias usa ainda um scope prÃ³prio do handler por padrÃ£o.

**ConclusÃ£o:** fechamento nÃ£o estÃ¡ conectado ao ciclo de vida no cÃ³digo localizado. A retenÃ§Ã£o concreta de Activity ou impacto em execuÃ§Ã£o nÃ£o foi medido.

**AÃ§Ã£o proposta:** preferir `applicationContext` para armazenamento; definir dono do scope/provider (Repository ou escopo de aplicaÃ§Ã£o), compartilhar uma instÃ¢ncia e cancelar/aguardar trabalhos conforme polÃ­tica de ciclo de vida. Cobrir recriaÃ§Ã£o da Activity e encerramento.

## Plano consolidado de correÃ§Ã£o e verificaÃ§Ã£o

1. Corrigir primeiro a composiÃ§Ã£o: provider Ãºnico, fonte de registry observÃ¡vel/atualizÃ¡vel e feedback de alias coerente com persistÃªncia concluÃ­da.
2. Fixar o contrato de carregamento: local vÃ¡lido, local ausente, local vazio, local malformado, asset vÃ¡lido e asset ausente.
3. Isolar o provider em testes com armazenamento e dispatcher/agendador controlÃ¡veis; garantir encerramento de providers nos fixtures.
4. Testar concorrÃªncia em duas camadas: integridade do cache sob chamadas simultÃ¢neas e ordem de commits ao arquivo com intercalaÃ§Ã£o controlada.
5. SÃ³ substituir/expandir o lock se testes mostrarem necessidade; manter I/O fora do lock sÃ­ncrono.
6. Executar testes JVM relevantes, testes instrumentados focados, suÃ­te instrumentada completa, lint e build; registrar resultados separados. Depois dos instrumentados sem falhas, fazer o smoke manual do aplicativo.

## LimitaÃ§Ãµes desta auditoria

- Achados de integraÃ§Ã£o e fluxo foram confirmados por inspeÃ§Ã£o estÃ¡tica; os novos casos propostos ainda nÃ£o foram executados.
- A reordenaÃ§Ã£o concorrente de gravaÃ§Ãµes e a retenÃ§Ã£o de Activity permanecem hipÃ³teses a medir.
- A inspeÃ§Ã£o inicial foi estÃ¡tica. CorreÃ§Ãµes posteriores e sua evidÃªncia estÃ£o registradas em `plano-acao-json-provider.md`; os itens de concorrÃªncia de gravaÃ§Ã£o e ciclo de vida continuam pendentes.
