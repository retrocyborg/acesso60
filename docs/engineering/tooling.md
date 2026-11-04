# Ferramentas e ambiente de desenvolvimento

Esta página registra configurações encontradas e verificações feitas no
repositório. “Configurado” indica presença de configuração; “executado” indica
que o comando foi rodado nesta revisão.

| Ferramenta | Uso | Situação observada |
|---|---|---|
| Kotlin 2.2.20 | Código Android e núcleo JVM | Configurado no build raiz. |
| Android Gradle Plugin 8.13.0 | Build Android | Configurado; `:app:assembleDebug` passou nesta revisão. |
| Gradle Wrapper 9.3.1 | Build reproduzível | Configurado; usado nesta revisão. Execute da raiz. |
| Android SDK | Compilar e instalar o app | `compileSdk 36`, `targetSdk 35`; build local passou. Teste em dispositivo não executado nesta revisão. |
| Jetpack Compose | Interface | Configurado; testes unitários passaram. Validação visual atual **A validar**. |
| SpeechRecognizer | Reconhecimento de fala | Adaptador Android implementado; entrada de fala real **A validar**. |
| AccessibilityService | Ações de acessibilidade | Adaptador implementado; ativação/uso completos no dispositivo **A validar**. |
| JUnit | Testes JVM | 36 testes do núcleo e 56 testes unitários Android passaram nesta revisão. |
| Android Lint | Análise Android | `:app:lintDebug` passou nesta revisão. |
| GitHub Actions | Build e testes remotos | Workflow configurado para o build raiz; execução remota **A validar**. |
| ktlint, detekt, Kover | Estilo, análise estática e cobertura | Não configurados no build inspecionado. |
| TextToSpeech | Feedback por voz | Não foi encontrada integração no código inspecionado. |
| GitBook e Figma | Documentação e protótipo acadêmico | Uso e estado atual **A validar**. |
| Codex | Apoio à leitura, revisão e documentação | Uso desta revisão descrito no rascunho `../reports/relatorio-uso-ia-rt03.md`; revisão humana ainda necessária. |

## Comandos usuais

Execute na raiz do repositório:

```powershell
.\gradlew.bat :core:test :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

Testes instrumentados exigem emulador ou dispositivo conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Não trate a configuração local ou um teste com doubles como prova de fala,
acessibilidade ou navegação real no Android.
