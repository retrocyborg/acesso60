# Nota de retomada — atalho Acesso60 no emulador

**Data:** 17/11/2026  
**Contexto:** sessão de teste do Acesso60 no AVD `Medium_Phone_API_37.0`.

## O que foi observado

- O APK debug foi instalado no emulador e a Activity principal foi iniciada via ADB.
- O código do launcher usa `ic_launcher`; tocar nesse ícone deve abrir `MainActivity`.
- O widget `Acesso60WidgetProvider` configura um `PendingIntent` que também abre `MainActivity`.
- O widget não inicia a escuta diretamente. A tela principal oferece o botão **Falar**, ligado a `viewModel::onListenRequested`.
- O relato de um ícone no canto inferior direito não foi confirmado visualmente; ainda é necessário distinguir ícone do launcher, widget ou outro elemento na tela.

## Pendência para retomar

1. Observar visualmente a tela do emulador e identificar o elemento descrito.
2. Tocar nele e registrar o resultado: abre a Activity, inicia escuta ou não responde.
3. Se a expectativa for iniciar escuta diretamente, decidir e implementar esse comportamento no fluxo do atalho, respeitando as permissões de microfone e os estados de acessibilidade.
4. Validar no emulador a abertura e, se implementado, o início da escuta; registrar logs e resultado sem declarar sucesso antes da execução.

**Status:** comportamento específico do ícone inferior direito **A validar**. Nenhuma alteração funcional foi feita nesta sessão.
