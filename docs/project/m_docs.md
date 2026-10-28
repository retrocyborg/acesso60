# Acesso60: arquitetura e suíte de testes

**Tipo de registro:** síntese técnica baseada na proposta do projeto e no estado inspecionado do repositório.  
**Fonte:** [Proposta, Arquitetura e Suíte de Testes — GitBook](https://trabalho-quarta.gitbook.io/acesso60-projeto-de-qualidade-de-software/proposta-arquitetura-e-suite-de-testes).  
**Estado:** arquitetura parcialmente implementada; extensibilidade por plugins proposta para a próxima etapa.

## 1. Contexto e objetivo

Pessoas idosas podem encontrar dificuldades em interfaces móveis que exigem várias etapas, leitura de elementos pequenos ou familiaridade com estruturas pouco conhecidas. O Acesso60 investiga uma camada de assistência por voz para tarefas Android selecionadas.

O objetivo é interpretar um conjunto controlado de comandos, encaminhá-los a mecanismos de execução apropriados e oferecer feedback com controle de risco. A voz complementa a navegação convencional; não se presume que todo aplicativo ou elemento visual possa ser controlado.

A redução da carga de navegação, a previsibilidade do parsing local e o efeito da confirmação sobre ações não intencionais são hipóteses a avaliar, não resultados comprovados neste documento.

## 2. Limites do MVP

O MVP não promete controle universal de aplicativos Android, automação de qualquer elemento visual, execução autônoma de ações sensíveis, armazenamento permanente de áudio ou conteúdo de tela, nem dependência obrigatória de serviço de IA externo.

O `AccessibilityService` depende do que cada aplicativo expõe, da versão do Android e da ativação pelo usuário. A arquitetura deve apresentar capacidades verificáveis e informar indisponibilidade quando não puder executar uma ação.

## 3. Arquitetura proposta

```text
UI / Widget
    ↓
Entrada de voz e Speech-to-Text
    ↓
Domínio de comandos
    ↓
Validação e política de risco
    ↓
Autorização / confirmação
    ↓
Roteamento
    ↓
Execução Android
    ↓
Verificação do resultado e feedback
```

| Camada | Responsabilidade |
|---|---|
| UI / Widget | Iniciar e cancelar interação; apresentar estados e feedback acessíveis. |
| Voice | Capturar áudio e converter fala em texto; comunicar erros e indisponibilidade. |
| Domínio de comandos | Normalizar texto, interpretar intenção e parâmetros e validar estrutura. |
| Risco e autorização | Classificar risco e exigir confirmação conforme a política central. |
| Roteamento | Escolher handler compatível após validação e autorização. |
| Execução | Adaptar comandos para APIs Android e `AccessibilityService`; retornar resultado tipado. |
| Verificação e feedback | Confirmar o efeito quando observável e informar sucesso, falha ou indisponibilidade. |
| Dados | Armazenar preferências e eventos operacionais necessários; retenção e minimização ainda precisam ser definidas. |

Direção de dependências:

```text
UI → ViewModel / Controller → domínio → abstrações ← adaptadores Android
```

As regras de domínio não devem depender de Compose, `SpeechRecognizer`, `Intent` ou `AccessibilityService`. O Android implementa adaptadores para os contratos do domínio. Modelos, interpretação, validação e risco devem ter uma única implementação compartilhada no núcleo Kotlin/JVM.

## 4. Extensibilidade por camadas e plugins

Para a próxima etapa, fica registrada a decisão proposta de priorizar extensibilidade dos comandos por camadas e plugins. A meta é permitir novos comandos com impacto localizado, mantendo validação, risco e autorização sob controle central do núcleo compartilhado.

Contratos candidatos para avaliação: `CommandDefinition`, `CommandRegistry` e `CommandHandler`. Uma definição poderá declarar identificador, aliases e parsing de parâmetros; um handler executará por uma abstração autorizada e devolverá resultado tipado. Nenhum plugin poderá substituir ou ignorar validação, política de risco ou confirmação.

O escopo inicial proposto é usar plugins internos para navegação, volume e abertura de aplicativos. Carregamento dinâmico ou plugins de terceiros não estão decididos. Assinatura, origem, permissões, compatibilidade, isolamento, ciclo de vida e regras de distribuição Android são **A validar**.

Critérios propostos para implementar a etapa:

1. Definir contratos e ciclo de vida sem dependência Android no núcleo.
2. Registrar aliases e parsers em registro determinístico, rejeitando colisões e definições inválidas.
3. Executar handlers somente após validação, avaliação de risco e autorização.
4. Retornar estados tipados para comando não suportado, serviço indisponível, recusa e falha.
5. Testar que plugins ausentes, inválidos ou com erro falham de forma segura e não executam ações não autorizadas.
6. Integrar Android por adaptadores e contratos testáveis.

Esses itens são planejamento, não funcionalidade implementada.

## 5. Política de risco e segurança

```text
Comando → validação → avaliação de risco → autorização → handler → resultado
                         ├─ baixo: conforme política aprovada
                         ├─ médio: confirmação quando exigida
                         └─ alto: confirmação explícita ou bloqueio
```

O comportamento final depende de política aprovada. Comando desconhecido, parâmetro ambíguo, falta de permissão ou ausência de serviço não podem chegar à execução. A confirmação deve estar vinculada ao comando e aos parâmetros apresentados ao usuário.

Uma eventual interpretação por IA pode sugerir uma intenção; não pode autorizar nem executar ações diretamente. Qualquer adoção dependerá de validação posterior e análise de privacidade, latência, custo e falhas.

## 6. Suíte de testes

A estratégia segue uma pirâmide: testes unitários determinísticos, testes de integração do pipeline, testes de UI para estados observáveis e poucos fluxos E2E representativos.

### Testes unitários

Isolar normalização, `CommandInterpreter`, `CommandValidator`, política de risco, registro e handlers quando implementados, e roteamento. Incluir entradas nulas e vazias, aliases, parâmetros ausentes, ambiguidades, comandos desconhecidos e falta de confirmação.

### Testes de integração

Validar o fluxo com executor fake:

```text
texto → normalização → interpretação → validação → risco/autorização → roteamento → executor fake
```

Comprovar que entradas desconhecidas, inválidas ou não autorizadas não chamam o executor. Para plugins, testar registro, colisões de aliases, contratos e falha isolada.

### Testes de UI

Verificar os estados e transições: `idle`, `listening`, `processing`, `confirmation`, `success` e `error`. Incluir cancelamento, erro de STT, serviço desligado e feedback acessível.

### Testes E2E

Manter poucos cenários em emulador/dispositivo: comando de navegação com serviço ativo, cancelamento de ação sensível, erro de reconhecimento e comando não suportado. Permissões e ativação do serviço devem ocorrer pelo fluxo permitido pelo Android. Mock ou teste unitário não comprova funcionamento real.

## 7. Matriz inicial de cenários

| ID | Cenário | Risco proposto na fonte | Resultado esperado |
|---|---|---|---|
| TST-001 | Voltar | Baixo | Executar se disponível e informar resultado. |
| TST-002 | Abrir aplicativo | Baixo | Abrir aplicativo resolvido ou informar indisponibilidade. |
| TST-003 | Ler tela | Baixo | Informar conteúdo acessível disponível, sem alegar leitura universal. |
| TST-004 | Aumentar volume | Baixo | Alterar volume quando handler e capacidade estiverem disponíveis. |
| TST-005 | Enviar mensagem | Alto | Exigir confirmação antes do envio; cancelamento não executa. |
| TST-006 | Comando desconhecido | — | Orientar recuperação e não executar. |
| TST-007 | Serviço desativado | — | Informar como habilitar ou que a ação está indisponível. |
| TST-008 | Falha de execução | — | Dar feedback honesto, sem stack trace. |

Os níveis de risco reproduzem a proposta de referência e devem ser confirmados antes de se tornarem política definitiva.

## 8. Quality gate proposto

```text
ktlint → detekt → Android Lint → testes unitários → integração/UI relevantes
       → Kover → code review → quality gate
```

A sequência é proposta pela fonte. A configuração e execução de cada ferramenta devem ser verificadas antes de declarar o gate ativo. Cobertura, latência e taxa de reconhecimento precisam de baseline e critérios acordados; não se atribuem metas sem decisão ou evidência.

## 9. Estado verificado no repositório

Na inspeção do código disponível:

- O núcleo contém `Command`, `CommandInterpreter`, `CommandValidator`, `RiskPolicy`, `CommandRouter` e a abstração `CommandExecutor`.
- O interpretador reconhece comandos fixos, incluindo voltar, início, leitura de tela, volume, abertura de aplicativo e uma ação sensível.
- O `AndroidCommandExecutor` inspecionado executa `Command.Back`; outros comandos retornam `ExecutionResult.Unavailable`.
- Existem testes JVM do núcleo e testes Android para executor, controlador e contratos; isso não comprova cobertura completa dos cenários desta proposta.
- Não foram encontrados `CommandRegistry` ou sistema de carregamento de plugins na implementação inspecionada.
- O status do projeto registra testes executados anteriormente e limitações de STT/áudio. Ambiente e resultados devem ser conferidos novamente antes de uma release ou declaração atualizada.

A arquitetura está parcialmente materializada por contratos e módulos. A execução Android continua incompleta para diversos comandos. Extensibilidade por plugins permanece **A validar**.

## 10. Evidência e rastreabilidade

Cada execução deve registrar data, branch/commit, ambiente, comando ou procedimento, resultado esperado, observado, status e limitações. Anomalias devem registrar impacto para o usuário, severidade justificada e evidência reproduzível.

- **Proposta:** intenção ainda não implementada ou validada.
- **Implementado:** código encontrado, sem alegar funcionamento integral.
- **Verificado:** execução observada e registrada com ambiente e resultado.
- **A validar:** lacuna de evidência ou decisão pendente.

## Referências

- GitBook: [Proposta, Arquitetura e Suíte de Testes](https://trabalho-quarta.gitbook.io/acesso60-projeto-de-qualidade-de-software/proposta-arquitetura-e-suite-de-testes), consultado em 28/10/2026.
- Repositório: `docs/project/architecture.md`, `docs/engineering/testing-strategy.md`, `docs/milestones/roadmap.md` e `docs/milestones/rt03-status.md`.

