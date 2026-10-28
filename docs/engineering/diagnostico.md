# Diagnóstico técnico — branch RT03

**Data da verificação:** 26/10/2026  
**Branch:** `RT03`  
**Commit analisado:** `799803312cc8d5298cc455f527bd4ff44feae281` (`estrutura inicial v1.0.0`)  
**Escopo:** estado versionado, alterações locais, documentação compactada e preparação da Beta do núcleo.

## 1. Objetivo e critério de certeza

Este diagnóstico verifica a estrutura efetivamente disponível para iniciar o
desenvolvimento do núcleo beta e separa:

- estado versionado da branch `RT03`;
- diferenças históricas em relação a `main`;
- arquivos presentes apenas no working tree;
- conteúdo documental do arquivo `Acesso60_documentacao_Codex.zip`.

Os termos usados neste relatório têm o seguinte significado:

- **Verificado:** observado por comando ou inspeção reproduzível nesta análise.
- **Planejado:** descrito na documentação, sem implementação comprovada.
- **Ausente no estado analisado:** não encontrado nos arquivos inspecionados.
- **A validar:** depende de uma fonte externa, como a matriz oficial da RT02,
  ou de decisão entre as frentes.

## 2. Estado do Git e da estrutura

### 2.1 Comparação entre `main` e `RT03`

Verificado:

- `RT03` está no commit `7998033`.
- `main` aponta para o commit `78e3233` (`Initial commit`).
- `RT03` adiciona documentação, diretórios de responsabilidade e o arquivo
  `Acesso60_documentacao_Codex.zip` em relação a `main`.
- Não há histórico de implementação Android entre esses commits.

### 2.2 Working tree local

No momento da verificação, havia:

- alteração em `Agents.md`;
- `BETA_NUCLEO_TODO.md` não rastreado;
- `docs/engineering/nucleo.md` não rastreado;
- `minhas_custom_instructions.md` não rastreado.

Esses arquivos fazem parte do estado local observado, mas não do conteúdo
versionado do commit analisado. A inclusão deles na branch ainda é uma decisão
de integração.

### 2.3 Componentes encontrados

As pastas `src/`, `tests/`, `config/`, `scripts/`, `assets/` e `docs/` existem.
No estado versionado, `src/` e `tests/` contêm somente `.gitkeep`.

Não foram encontrados:

- arquivos `.kt`, `.java` ou `.kts` de aplicação;
- `gradlew` ou `gradlew.bat`;
- `settings.gradle` ou `settings.gradle.kts`;
- `build.gradle` ou `build.gradle.kts`;
- `AndroidManifest.xml`;
- `pom.xml`, `package.json` ou outro manifesto executável;
- testes automatizados implementados.

## 3. Build, ambiente e testes

### Evidência observada

- O executável `java.exe` está disponível.
- A versão observada foi Java `24.0.2`.
- Não foram localizados Gradle, Gradle Wrapper, `adb` ou `kotlinc` no PATH
  durante a verificação.
- Não existe projeto executável no estado analisado para realizar build ou
  teste automatizado.

### Conclusão

Build, testes unitários, lint e execução Android estão **não verificáveis**
nesta etapa. Não há evidência para declarar compilação, cobertura, teste
passando ou integração com dispositivo.

## 4. Conteúdo do ZIP documental

O arquivo `Acesso60_documentacao_Codex.zip` contém `README.md`, `AGENTS.md` e
documentos em `docs/`, mas não contém código Android nem APK.

O `README.md` interno declara que:

- o estado do código não foi auditado naquela entrega documental;
- o pacote contém documentação, não o aplicativo Android nem um APK;
- a execução e o Gradle Wrapper estavam pendentes de auditoria.

Os hashes dos documentos do ZIP coincidem com os documentos atuais em `docs/`.
As diferenças observadas estão concentradas em `AGENTS.md` e `README.md`.

O ZIP deve ser tratado como referência documental, não como fonte de código a
ser incorporada automaticamente.

## 5. Situação do núcleo beta

### Estado verificado

Os seguintes componentes estão descritos na documentação, mas não foram
encontrados como implementação:

- `Command`;
- `CommandInterpreter`;
- `CommandValidator`;
- `RiskPolicy`;
- `CommandRouter`;
- executor desacoplado;
- testes `UT-01` a `UT-05`;
- teste de integração `IT-01`.

### Contratos necessários antes do código

As frentes devem acordar, antes da implementação:

- modelo tipado de comando;
- resultado de interpretação;
- resultado de validação;
- classificação de risco;
- necessidade de confirmação/autorização;
- estados de execução;
- resultado do executor (`Success`, `Unavailable`, `Denied` e `Failure`, ou
  equivalentes aprovados).

O núcleo deve receber texto reconhecido e não deve depender diretamente de
microfone, `SpeechRecognizer`, `AccessibilityService`, `Intent`, Compose ou
feedback visual.

## 6. Requisitos e rastreabilidade

Os IDs `A60-RF-*` presentes em `docs/engineering/requirements.md` são
identificados como internos e provisórios. A vinculação com os IDs formais da
RT02 ainda está **A validar**.

Para o beta do núcleo, a rastreabilidade mínima planejada é:

| Comportamento | Componente | Teste | Estado |
|---|---|---|---|
| “voltar” → `Command.Back` | `CommandInterpreter` | `UT-01` | Planejado |
| “início”/“inicio” → `Command.Home` | `CommandInterpreter` | `UT-02` | Planejado |
| desconhecido não executa | Interpreter/Validator | `UT-03` | Planejado |
| vazio/inválido é rejeitado | `CommandValidator` | `UT-04` | Planejado |
| sensível exige confirmação | `RiskPolicy` | `UT-05` | Planejado |
| fluxo autorizado com executor falso | Router e executor | `IT-01` | Planejado |

## 7. Bloqueios e riscos

### Bloqueadores da Beta

1. Não há projeto executável ou estrutura Gradle verificada.
2. Não há implementação do núcleo.
3. Não há testes executáveis.
4. Os contratos entre as frentes ainda não foram formalmente aprovados.
5. Os requisitos da RT02 ainda não foram confrontados com a fonte oficial.

### Riscos de integração

- As frentes Android e Interface podem criar modelos incompatíveis se os
  contratos forem definidos separadamente.
- A execução Android pode ser acoplada indevidamente ao interpretador.
- Uma futura IA pode ser tratada como autorizadora em vez de fornecedora de
  intenção candidata.
- Arquivos não rastreados podem ser confundidos com entregáveis já integrados
  à branch.

### Riscos acadêmicos da RT03

Ainda não há evidência verificável de:

- protótipo interativo ou alfa/beta demonstrável;
- testes unitários executados;
- tratamento de erros demonstrado;
- relatório de uso de IA;
- slides e ensaio da banca;
- build ou execução Android.

## 8. Roadmap após o diagnóstico

### P0 — desbloqueio

1. Confirmar a fonte oficial da RT02 e atualizar a rastreabilidade.
2. Formalizar os contratos do núcleo com as outras frentes.
3. Definir ou recuperar a estrutura executável Android/Gradle.
4. Implementar `Command`, `CommandInterpreter`, `CommandValidator`, `RiskPolicy`
   e `CommandRouter` sem dependências Android.
5. Implementar `UT-01` a `UT-05` e `IT-01` com executor falso.
6. Registrar os resultados e as limitações.

### P1 — integração RT03

1. Integrar o texto reconhecido da frente Android.
2. Integrar estados de confirmação e feedback da Interface.
3. Criar adaptador de execução Android.
4. Demonstrar fluxo autorizado, erro tratado e ação sensível bloqueada.
5. Consolidar relatório de IA e roteiro da banca.

### P2 — evolução posterior

- fallback de interpretação com IA;
- abertura de aplicativos;
- volume e leitura de tela;
- métricas formais, CI, cobertura e publicação do APK.

## 9. Comandos e evidências

Comandos utilizados nesta verificação:

```text
git branch --show-current
git rev-parse HEAD
git status --short
git log --oneline --decorate -10
git diff --stat main...RT03
git ls-tree -r --name-only HEAD
rg --files -g 'gradlew*' -g 'settings.gradle*' -g 'build.gradle*' \
  -g 'AndroidManifest.xml' -g '*.kt' -g '*.java' -g '*Test*'
```

O ZIP foi aberto em diretório temporário para inspeção somente leitura. O
diretório temporário não faz parte do repositório.

## 10. Limitações

- O diagnóstico não acessou o estado remoto do GitHub nem confirmou permissões
  ou branches remotas além das referências locais disponíveis.
- A matriz oficial da RT02 não está presente no estado analisado.
- Não foi possível verificar build ou testes porque a estrutura executável não
  está disponível nesta branch.
- A ausência local de código não prova que outra frente ou repositório não
  possua uma implementação paralela.
