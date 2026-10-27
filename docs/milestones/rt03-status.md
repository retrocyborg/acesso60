# RT03 — status e evidências

**Última consolidação de conversas:** 26/10/2026. **Auditoria do código:** não realizada neste pacote.

## Regra de status
`Não verificado` significa ausência de inspeção/evidência neste material, **não** afirma que a equipe não fez a atividade.

| Entregável RT03 | Situação confirmável a partir da conversa | Evidência ainda necessária |
|---|---|---|
| Documentação consolidada | Conteúdo-base reunido neste pacote | Comparar com documentos oficiais do GitBook |
| Protótipo interativo / alfa-beta | Telas e fluxo planejados | Link navegável, vídeo, APK ou demo |
| Testes unitários iniciais | Casos especificados | Código de testes + resultado real |
| Validação e tratamento de erros | Regras propostas | Execução mostrando recusa/falha amigável |
| Relatório de uso de IA | Estrutura e exigências identificadas | Exemplos reais, revisões e riscos documentados |
| Roteiro de apresentação | Esqueleto proposto em `rt03-plan.md` | Slides e ensaio |
| Repositório GitHub | URL de referência registrada | Verificar branches, commits, fontes e permissões |
| Ferramentas QA | Selecionadas | Execução de build/linters/testes/cobertura |

## Registro de verificações futuras
Ao realizar cada verificação, adicionar entrada neste formato:

```text
Data:
Commit / branch:
Responsável:
Requisito / artefato:
Ambiente:
Comando / procedimento:
Resultado esperado:
Resultado observado:
Status: passou | falhou | bloqueado
Link de evidência:
Limitação:
```

## Bloqueios históricos
Tentativas de gravação no repositório por integração receberam resposta 403 (`Resource not accessible by integration`). Isso não prova que o repositório permaneça inacessível hoje.

## Pendências de auditoria prioritárias
1. Repositório clona? Contém Gradle Wrapper e manifesto?
2. Projeto compila? Se não, registrar erro original.
3. Quais componentes existem e quais são apenas stubs?
4. Quais testes rodam realmente?
5. Há logs e imagens de evidência para a apresentação?
6. Qual artefato RT03 será apresentado: Figma de alta fidelidade, alfa ou beta?

**Proibição:** nunca alterar este arquivo para marcar sucesso sem evidência concreta.
