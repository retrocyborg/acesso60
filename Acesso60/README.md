# Módulo Android

Esta pasta contém o aplicativo Android do Acesso60. O módulo Gradle `:app`
usa o diretório `Acesso60/app` e é compilado pelo build definido na raiz do
repositório.

## Compilar

Execute os comandos a partir da raiz do repositório:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Consulte o [README principal](../README.md) para entender o projeto e o
[índice da documentação](../docs/INDEX.md) para localizar arquitetura,
requisitos, testes e status RT03.
