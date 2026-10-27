# Acesso60

Aplicativo Android de acessibilidade e assistência por voz.

## Stack inicial
- Kotlin
- Jetpack Compose
- Android AccessibilityService
- SpeechRecognizer / camada de Speech-to-Text
- Intent / Command Parser

## Arquitetura
Speech-to-Text → Command Interpreter → Validation/Risk Policy → Command Router → Execution → Verification → Feedback

## Objetivo do MVP
Construir uma camada de assistência que permita ao usuário executar comandos básicos do smartphone por voz, com validação, controle de risco e feedback.

## Estrutura inicial
- `command/`: interpretação, validação e política de risco
- `voice/`: abstração de reconhecimento de voz
- `accessibility/`: integração com AccessibilityService
- `ui/`: interface Jetpack Compose
