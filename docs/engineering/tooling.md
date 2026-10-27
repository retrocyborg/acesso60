# Ferramentas e ambiente de desenvolvimento

> **Estado reportado:** ferramentas escolhidas/discutidas, não instalação verificada. Ao auditar repositório, atualizar coluna de evidências.

| Ferramenta | Papel | Situação atual |
|---|---|---|
| Kotlin | Linguagem Android | Escolhida |
| Jetpack Compose | UI declarativa | Escolhida |
| Android Studio/SDK | Desenvolvimento, emulador, build | Ambiente previsto |
| Gradle + Kotlin DSL | Build e dependências | Esqueleto conceitual citado; verificar arquivos |
| Android SpeechRecognizer | STT | Integração planejada |
| Android TextToSpeech | Feedback por voz | Planejado |
| AccessibilityService | Ações de acessibilidade | Planejado; exige configuração/permissão apropriada |
| Git + GitHub | Histórico, colaboração | Repositório referenciado, escrita não comprovada |
| GitBook | Documentação | Plataforma do trabalho |
| Figma | Protótipo | Selecionado |
| JUnit | Testes unitários | Selecionado |
| Compose UI Test | Testes de interface | Selecionado |
| Android Lint | Diagnóstico Android | A executar/verificar |
| ktlint | Estilo Kotlin | A configurar/verificar |
| detekt | Análise estática | A configurar/verificar |
| Kover | Cobertura | A configurar/verificar |
| GitHub Actions | CI | A configurar/verificar |
| ChatGPT/Codex | Apoio a planejamento e código | Uso/documentação em andamento |

## Verificação do ambiente (após clonar o repositório)
```bash
ls -la
find . -maxdepth 3 -type f | sort | head -100
./gradlew tasks                # somente se gradlew existir
./gradlew test                 # somente se tarefa configurada
./gradlew lint                 # somente se tarefa configurada
```
Não presumir disponibilidade de `ktlintCheck`, `detekt` ou `koverXmlReport` sem inspecionar plugins e `./gradlew tasks`.

## CI proposto
Commit/PR → validação do Gradle → Android Lint → testes de domínio → ktlint/detekt se integrados → cobertura Kover se integrada → relatório do PR.

## Referências úteis
- Codex e AGENTS.md: https://learn.chatgpt.com/docs/agent-configuration/agents-md
- Android AccessibilityService: https://developer.android.com/guide/topics/ui/accessibility/service
- Android speech recognition: https://developer.android.com/reference/android/speech/SpeechRecognizer
