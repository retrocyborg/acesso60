# Atualização: abrir qualquer app instalado e enviar mensagem direto

Versão do app: 1.2.0 (versionCode 2).

## O que mudou

### 1. "Abrir <app>" lê os APKs instalados
- `InstalledAppsSource` / `AndroidInstalledAppsSource`: consulta o `PackageManager` (ACTION_MAIN + CATEGORY_LAUNCHER) e obtém nome visível + `packageName` de cada app. Cache de 30 s.
- `StaticAppRegistry` resolve o nome falado em duas etapas: alias do `app_definitions.json` e, se não achar, nome visível dos apps instalados (`AppNameMatcher`: ignora acento/caixa; "youtube" abre "YouTube", não "YouTube Music").
- Se o alias aponta para um pacote que não está instalado (ex.: app de mensagens da Samsung), usa o app instalado com o mesmo nome.
- `CommandInterpreter` entende "abrir", "abre", "abra", "iniciar", "inicie", "executar" + artigos e "aplicativo/app" ("abre o youtube", "abrir aplicativo whatsapp", "abrir google maps por favor").
- `AndroidManifest.xml`: `<queries>` com ACTION_MAIN/CATEGORY_LAUNCHER (necessário no Android 11+).
- `app_definitions.json` ampliado (25 apps, 67 aliases). O arquivo salvo pelo usuário agora é MESCLADO com os assets (antes, ele escondia os assets para sempre).

### 2. Mensagem enviada direto, sem tela de confirmação
- `RiskPolicy`: `OpenApp` e `SendMessage` passam a `LowRisk`. `ReadScreen` e `SensitiveAction` continuam exigindo confirmação.
- `VoiceMessageCoordinator`: aceita a frase inteira ("mandar mensagem para a Maria pelo WhatsApp dizendo chego em 10 minutos"), também "manda/mande/envia/envie"; só pergunta o que faltou. Mensagem final: "Enviando mensagem para X no Y."
- `AndroidContactResolver` + `ContactMatcher`: acha o telefone na agenda (permissão READ_CONTACTS). Nome ambíguo entre pessoas diferentes (dois "João") NÃO é adivinhado: cai no modo manual.
- `MessageIntentFactory`: com telefone, abre a conversa já no contato (`api.whatsapp.com/send?phone=...&text=...` ou `smsto:`); sem telefone, ACTION_SEND só com o texto.
- Envio automático: `AutoSendRequests` + `Acesso60AccessibilityService` tocam em "Enviar" (`SendButtonMatcher`). Só clica se (1) o Acesso60 pediu há menos de 15 s, (2) a janela é do app esperado, (3) um clique por pedido.
- Telegram e Signal não têm link de conversa com texto: abrem com o texto pronto e a pessoa escolhe o contato e toca em enviar (sem clique automático, de propósito).

## Como testar no aparelho
1. `./gradlew test assembleDebug` e instale `Acesso60/app/build/outputs/apk/debug/app-debug.apk`.
2. Ative Configurações > Acessibilidade > Acesso60 e permita Contatos e Microfone.
3. Diga "abrir youtube" (abre direto). Diga "mandar mensagem para <contato>", "WhatsApp", e a mensagem: deve enviar sem pedir confirmação.

## Limites conhecidos
- O botão "Enviar" depende dos ids/rótulos do app de mensagens; se uma versão futura do WhatsApp mudar o botão, o app abre a conversa com o texto pronto e o toque final volta a ser manual (aviso de 15 s sem clique).
- O resultado é sempre "não verificado": quem entrega a mensagem é o app externo.
