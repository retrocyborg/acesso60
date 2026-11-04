# Núcleo Beta — Requisitos e contratos

## Contexto

Este documento define o escopo da frente **Núcleo Beta** na branch `RT03`.
O desenvolvimento ocorre em paralelo com as frentes de interface e Android;
por isso, os contratos deste documento devem permanecer desacoplados de UI,
microfone e APIs específicas do Android.

Referência operacional: `BETA_NUCLEO_TODO.md`.

## Obrigação da RT03

Para esta frente, a RT03 exige evidências iniciais de qualidade, incluindo:

- validação de entradas;
- tratamento controlado de erros;
- testes unitários básicos executáveis;
- demonstração de que ações sensíveis não são executadas sem autorização;
- documentação dos contratos e das limitações do núcleo.

A comprovação final dos critérios acadêmicos, métricas completas e integração
Android publicada permanece dependente do escopo da RT04.

## Escopo da versão beta

O núcleo beta deve conter:

- modelo tipado de `Command`;
- `CommandInterpreter`;
- `CommandValidator`;
- `RiskPolicy`;
- `CommandRouter`;
- interface de executor desacoplada;
- testes unitários e teste de integração com executor falso.

## Requisitos funcionais do núcleo

### Interpretação

- `"voltar"` deve resultar em `Command.Back`.
- `"início"` e `"inicio"` devem resultar em `Command.Home`.
- Texto desconhecido deve retornar uma falha controlada, sem gerar comando
  executável.
- A normalização deve tratar diferenças de caixa e acentuação sem ampliar
  silenciosamente o conjunto de comandos suportados.

### Validação

- Texto vazio, nulo ou inválido deve ser rejeitado.
- Comandos desconhecidos devem ser rejeitados.
- Parâmetros ausentes ou incompletos devem ser rejeitados.
- A rejeição deve ser representada por um resultado tipado e não deve iniciar
  execução.

### Política de risco

- Operações simples, como navegação básica, podem ser classificadas como baixo
  risco conforme a política aprovada.
- Operações sensíveis devem exigir confirmação ou ser bloqueadas.
- Ausência de autorização, intenção ou contexto suficiente deve resultar em
  negação segura.
- A política de risco não deve executar ações nem modificar dados do usuário.

### Roteamento e execução

- O `CommandRouter` deve receber somente comandos validados e autorizados.
- O roteador deve selecionar o executor compatível sem interpretar texto.
- O executor deve retornar resultado estruturado, por exemplo:
  `Success`, `Unavailable`, `Denied` ou `Failure`.
- O mesmo comando não deve ser roteado mais de uma vez pelo fluxo normal.

## Contratos para acoplamento futuro

Os contratos devem permitir que outras frentes sejam conectadas sem alterar a
lógica de domínio:

| Consumidor ou fornecedor | Contrato esperado |
|---|---|
| Frente Android/STT | Entrega texto reconhecido ou erro estruturado |
| Núcleo | Produz comando tipado, rejeição, risco e decisão de execução |
| Frente de interface | Consome estados de confirmação, sucesso, erro ou cancelamento |
| Adaptador Android | Implementa o executor autorizado |
| Testes | Substituem STT, executor e feedback por fakes |

O núcleo não deve importar ou chamar diretamente:

- `SpeechRecognizer` ou microfone;
- `AccessibilityService`;
- `Intent` ou APIs de navegação Android;
- UI Compose;
- serviços de feedback visual ou TTS.

Uma futura resposta de IA poderá sugerir uma intenção, mas deverá passar pelos
mesmos interpretador, validador, política de risco e autorização. A IA nunca
deve conceder autorização nem executar comandos diretamente.

## Testes mínimos

| ID | Cenário | Resultado esperado |
|---|---|---|
| UT-01 | `"voltar"` | `Command.Back` |
| UT-02 | `"início"`/`"inicio"` | `Command.Home` |
| UT-03 | Frase desconhecida | Falha controlada, sem comando executável |
| UT-04 | Texto vazio ou inválido | Rejeição pelo validador |
| UT-05 | Ação sensível | Confirmação obrigatória ou bloqueio |
| IT-01 | Interpreter → Validator → RiskPolicy → Router | Executor falso recebe somente fluxo autorizado |

## Definition of Done

O núcleo beta poderá ser considerado implementado somente quando houver:

- comandos suportados mapeados de forma determinística;
- entradas vazias, inválidas e desconhecidas rejeitadas;
- ações sensíveis impedidas sem autorização;
- separação entre domínio, UI e APIs Android;
- execução dos testes `UT-01` a `UT-05` e `IT-01`;
- registro do comando, resultado observado e evidência de cada teste;
- limitações e pendências documentadas.

## Fora do escopo desta frente

Não fazem parte desta implementação, salvo alinhamento entre as branches:

- `SpeechToTextEngine`;
- `AccessibilityService`;
- `MainActivity`;
- telas Compose;
- execução real em dispositivo Android;
- fallback de interpretação com IA em runtime.

## Evidência e status

Cada item deve ser registrado com:

```text
ID da tarefa:
Requisito RT02 vinculado: A validar com a matriz oficial
Arquivos alterados:
Branch / commit:
Comando ou procedimento de teste:
Resultado observado:
Evidência:
Pendências:
```

Os requisitos deste documento são uma especificação proposta para a frente.
Nenhum item deve ser declarado implementado ou verificado sem código,
execução e evidência reproduzível.
