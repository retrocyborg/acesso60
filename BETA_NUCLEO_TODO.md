# Acesso60 — TODO Beta · Núcleo de comandos

> **Referência:** divisão fiel do [`Acesso60_Beta_TODO.md`](../Acesso60_Beta_TODO.md) de 26/10/2026.  
> **Marco:** RT03 — 28/10/2026; **repositório:** https://github.com/retrocyborg/Acesso.  
> **Status:** todos os itens permanecem `[ ]` até verificação por código, teste e evidência.  
> **Prioridades:** P0 = bloqueia a Beta proposta; P1 = complementar; P2 = evolução posterior.  
> **Trabalho em paralelo:** não editar arquivos pertencentes a outras frentes sem alinhar contrato e branch.

**Escopo desta frente:** interpretador, validador, política de risco, roteamento e testes de regras de negócio.

## Pré-condições compartilhadas (antes de desenvolver em paralelo)

- [ ] **BETA-00.1** — Inspecionar branch/commit e estrutura real do repositório; registrar componentes ausentes.
- [x] **BETA-00.2** — Verificar compilação e registrar log Gradle, JDK e SDK ou impedimentos.
- [ ] **BETA-00.3** — Vincular tarefas aos RF/RNF reais da RT02, sem inventar identificadores.
- [x] **CONTRATO-01** — Acordar modelo `Command`, resultado da validação, classificação de risco, estados e resultados de execução; definir interfaces antes de implementar consumidores.

**Responsabilidade:** atividade conjunta de diagnóstico/integração. Cada frente usa os contratos aprovados; mudanças neles exigem coordenação.

## Tarefas atribuídas

### P0 — Lógica de domínio

- [x] **BETA-04** — Implementar e testar `CommandInterpreter`.
  - **Aceite:** normalizar texto e reconhecer os comandos apoiados; entradas desconhecidas retornam resultado controlado.

- [x] **BETA-05** — Implementar `CommandValidator`.
  - **Aceite:** recusar comandos nulos, desconhecidos ou incompletos sem provocar execução.

- [x] **BETA-06** — Implementar `RiskPolicy`.
  - **Aceite:** diferenciar operações simples de sensíveis; não executar operação sensível sem confirmação.

- [x] **BETA-07** — Implementar `CommandRouter` e executor desacoplados do interpretador.
  - **Aceite:** interpretação não chama APIs Android diretamente; fluxo passível de teste com executores falsos.

### P0 — Testes de domínio e integração

- [x] **UT-01** — `"voltar"` resulta em `Command.Back`.

- [x] **UT-02** — `"início"` / `"inicio"` resulta em `Command.Home`.

- [x] **UT-03** — frase desconhecida não gera comando executável.

- [x] **UT-04** — texto vazio/inválido é rejeitado pelo validador.

- [x] **UT-05** — ação sensível exige confirmação pela política de risco.

- [x] **IT-01** — integração `Interpreter → Validator → RiskPolicy → Router` com executor simulado.

### P2 — Evolução relacionada

- [ ] **P2-01** — Fallback de interpretação com IA sob as mesmas regras de validação, risco e autorização.

## Integração e dependências

- Recebe **texto reconhecido** da frente Android, sem acesso direto ao microfone.
- Expõe comandos tipados, rejeições estruturadas, risco e interface para executor; **não chama** `AccessibilityService`, `Intent` nem UI Compose.
- A frente Interface consome estados de confirmação e resultado; a frente Android implementa adaptadores para execução real.
- Os testes IT-01 devem utilizar executor falso, sem dispositivo.

## Definition of Done da frente

- [ ] Comandos suportados mapeados de modo determinístico.
- [ ] Entradas vazias, inválidas e desconhecidas rejeitadas.
- [ ] Comando sensível não chega à execução sem autorização.
- [ ] Interpretador desacoplado das APIs Android e da UI.
- [ ] UT-01 a UT-05 e IT-01 executados com resultados registrados.

## Limites de propriedade e colaboração

- **Arquivos típicos:** `command/Command.kt`, `CommandInterpreter.kt`, `CommandValidator.kt`, `RiskPolicy.kt`, `CommandRouter.kt` e testes correspondentes.
- Não editar `SpeechToTextEngine`, `AccessibilityService`, `MainActivity` ou telas Compose sem alinhamento.

## Modelo de evidência

```text
ID da tarefa:
Requisito RT02 vinculado:
Arquivos alterados:
Branch / commit:
Comando ou procedimento de teste:
Resultado observado:
Evidência:
Pendências:
```

### Registros de Evidência

```text
ID da tarefa: BETA-00.2
Requisito RT02 vinculado: A validar com a matriz oficial
Arquivos alterados: build.gradle.kts, settings.gradle.kts, gradlew, gradlew.bat, gradle/, .gitignore
Branch / commit: RT03 / uncommitted (working tree local)
Comando ou procedimento de teste: .\gradlew.bat test
Resultado observado: BUILD SUCCESSFUL in 22s (tasks: jar, compileKotlin, test - Gradle 8.14.6, JDK 24.0.2 com fallback JVM 22, JUnit 5 configurado)
Evidência: Execução bem-sucedida do Gradle Wrapper local
Pendências: Implementar código-fonte e testes do domínio (UT-01 a UT-05, IT-01)
```

```text
ID da tarefa: CONTRATO-01, BETA-04, BETA-05, BETA-06, BETA-07, UT-01, UT-02, UT-03, UT-04, UT-05, IT-01
Requisito RT02 vinculado: A validar com a matriz oficial
Arquivos alterados: src/br/com/acesso60/core/model/Command.kt, src/br/com/acesso60/core/model/CoreResults.kt, src/br/com/acesso60/core/interpreter/CommandInterpreter.kt, src/br/com/acesso60/core/validator/CommandValidator.kt, src/br/com/acesso60/core/policy/RiskPolicy.kt, src/br/com/acesso60/core/executor/CommandExecutor.kt, src/br/com/acesso60/core/router/CommandRouter.kt, tests/br/com/acesso60/core/interpreter/CommandInterpreterTest.kt, tests/br/com/acesso60/core/validator/CommandValidatorTest.kt, tests/br/com/acesso60/core/policy/RiskPolicyTest.kt, tests/br/com/acesso60/core/integration/IntegrationPipelineTest.kt
Branch / commit: RT03 / uncommitted (working tree local)
Comando ou procedimento de teste: .\gradlew.bat test
Resultado observado: BUILD SUCCESSFUL in 40s (13 testes executados em 4 suítes, 0 falhas, 0 erros, 0 ignorados)
Evidência: Relatório XML de testes em build/test-results/test/ e logs da execução
Pendências: Integração P1 com frentes Android (STT real) e UI Compose
```

> Não assinalar `[x]` sem evidência verificável. A confirmação global da Beta exige a integração das três frentes e a documentação acadêmica da RT03.
