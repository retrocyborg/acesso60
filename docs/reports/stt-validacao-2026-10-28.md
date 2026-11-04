# Validacao do STT - 28/10/2026

## Estado da implementacao

O projeto possui `SpeechToTextEngine`, `RecognizerClient`,
`AndroidRecognizerClient` e `AndroidSpeechToTextEngine`. O adaptador usa o
`SpeechRecognizer` Android, idioma `pt-BR`, resultados parciais e no maximo um
resultado final.

## Testes modulares executados

`AndroidSpeechToTextEngineTest` cobre:

- resultados parciais e finais nao vazios;
- normalizacao de espacos;
- descarte de resultados vazios;
- conversao de erro Android em erro generico;
- reconhecedor indisponivel;
- delegacao de start, stop, cancel e destroy.

## Validacao ponta a ponta

Bloqueada nesta execucao: nao existe arquivo de audio pre-gravado no
workspace, e nenhum arquivo foi anexado para encaminhamento ao microfone do
emulador. Portanto, nao foi declarado reconhecimento real de `voltar` ou
qualquer outro comando.

## Proximo procedimento

Com um arquivo fornecido, registrar formato, duracao e hash; conceder
`RECORD_AUDIO`; confirmar disponibilidade do reconhecedor; encaminhar o audio
pela rota de entrada do dispositivo; e observar resultado parcial, resultado
final, transcricao na UI e evento enviado ao interpretador.

## Classificacao

- STT modular: implementado e verificavel por testes JVM.
- STT real com audio: **A validar**.
- Causa da pendencia: ausencia do arquivo de entrada e da evidencia de audio
  chegando ao `SpeechRecognizer`.

## Diagnostico implementado

O contrato de voz agora preserva uma classificacao de falha (`SpeechFailure`)
para diferenciar reconhecedor indisponivel, audio indisponivel, ausencia de
fala, idioma indisponivel, rede, reconhecedor ocupado e permissao. A UI recebe
mensagens amigaveis correspondentes, sem expor o codigo tecnico do Android.

Validacoes apos a alteracao:

- `:core:test`: passou;
- `:app:testDebugUnitTest`: passou;
- `:app:lintDebug`: passou;
- `:app:assembleDebug`: passou;
- `:app:connectedDebugAndroidTest`: 5/5 passaram.

A classificacao melhora a observabilidade, mas nao transforma um ambiente sem
entrada de audio em validacao de STT real.

O leitor de estado tambem exige que o servico de acessibilidade esteja
conectado, e nao apenas marcado nas configuracoes do Android. Isso evita
apresentar uma capacidade operacional que ainda nao foi estabelecida.

## Correcao da checagem de entrada - 28/10/2026

Foi adicionada uma sondagem da rota de entrada Android antes de iniciar o
`SpeechRecognizer`. Quando `AudioManager` nao informa nenhum dispositivo de
entrada, o engine retorna `SpeechFailure.AUDIO_UNAVAILABLE` e nao chama
`startListening()`. O adaptador tambem registra o codigo de erro Android e o
locale `pt-BR` no logcat, sem registrar audio bruto.

Validacao executada:

- `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`: passou;
- `:app:connectedDebugAndroidTest`: 5/5 testes passaram no AVD API 37;
- `media.audio_flinger`: `Input device: 0 (AUDIO_DEVICE_NONE)`;
- `accessibility`: `Bound services: {}` apos reinstalacao do APK.

Conclusao: a deteccao de rota foi implementada e testada, mas o STT real com
USB Audio Device 4 permanece **A validar**. O bloqueio atual esta na ponte de
audio do emulador e na reconexao efetiva do servico de acessibilidade, nao na
compilacao ou nos testes modulares do engine.

Tambem foi corrigida a leitura do estado de acessibilidade: a configuracao e
lida de `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` e a conexao real e
publicada pelo proprio `AccessibilityService` em um `StateFlow`. A UI distingue
desligado, conectando, conectado e falha de conexao apos 10 segundos.

Nova validacao: build, lint e `:app:connectedDebugAndroidTest` passaram
novamente; 5/5 testes instrumentados foram aprovados.

O teste manual confirmou `Bound services` e `Enabled services` para o Acesso60.
Uma captura de tela sem `uiautomator` mostrou `Servico de acessibilidade:
Ligado`. O `uiautomator dump` desconecta e reconecta temporariamente servicos
de acessibilidade; por isso ele nao deve ser usado sozinho como evidencia do
estado visual durante este caso.

## Evidencia da ponte de audio - 28/10/2026

Foi criado `scripts/audio/diagnose-emulator-input.ps1` e um capturador
`AudioRecord` exclusivo do build debug. O `tinycap` via shell foi descartado
como prova porque o Android negou acesso a `/dev/snd/pcmC0D0c`.

A captura interna do aplicativo produziu um WAV PCM mono, 48 kHz, 16 bits, com
5 segundos, 240000 amostras e 480044 bytes. Foram observadas 110603 amostras
nao nulas, pico normalizado `0.00811768` e RMS normalizado `0.00078209`.
SHA-256: `99E03E646E223E5996DFE54A35D011BF0B28F7449A5A1965667C580DA0653B65`.

Conclusao atualizada: a ponte do microfone do host para o Android entrega
amostras reais. O sinal observado e muito baixo e o reconhecedor online ainda
retornou `NO_SPEECH_DETECTED`, apesar de abrir o microfone e detectar inicio e
fim de fala. A pendencia agora e nivel/qualidade do sinal e reconhecimento, e
nao ausencia absoluta de rota.

Evidencia temporaria: `%TEMP%\acesso60-audio-diagnostics\20261028-055602`.
