# Agents.md

Este arquivo define as regras obrigatórias para qualquer agente de IA que
trabalhe neste repositório.

## Hook obrigatório de estrutura

Antes de criar ou mover qualquer arquivo, o agente deve:

1. Ler este arquivo e verificar a árvore atual do projeto.
2. Identificar a categoria do artefato que será criado.
3. Criar a pasta-pai prevista para essa categoria, caso ela ainda não exista.
4. Só então criar o arquivo dentro da pasta correta.
5. Confirmar a árvore resultante e verificar se não há arquivos fora do lugar.

Se a categoria não estiver prevista, o agente deve atualizar este arquivo antes
de criar a nova pasta ou arquivo.

## Estrutura de pastas

- `src/`: código-fonte da aplicação.
- `tests/`: testes automatizados e seus fixtures.
- `docs/`: documentação do projeto e decisões técnicas.
- `scripts/`: scripts de desenvolvimento, manutenção e automação.
- `config/`: arquivos de configuração não sensíveis.
- `assets/`: recursos estáticos usados pela aplicação.

Arquivos na raiz devem ficar limitados a documentação geral, metadados do
projeto e arquivos de configuração essenciais (por exemplo, `README.md`,
`Agents.md` e manifests). Não criar código de aplicação diretamente na raiz.

## Sequência de criação

Ao iniciar uma nova área do projeto, siga esta ordem:

1. Criar a pasta-pai da área.
2. Criar subpastas somente quando houver um artefato que as justifique.
3. Criar o arquivo dentro da pasta-pai ou subpasta correspondente.
4. Atualizar a documentação relevante.
5. Validar a estrutura com uma listagem da árvore e revisar o diff.

## Regras gerais

- Não usar pastas genéricas como `misc`, `stuff` ou `new`.
- Não duplicar arquivos entre áreas; colocar cada arquivo no local de sua
  responsabilidade principal.
- Não mover ou remover arquivos existentes sem verificar seu uso e registrar a
  mudança.
- Nunca gravar segredos em arquivos versionados.
- Manter nomes de pastas e arquivos claros, consistentes e em minúsculas,
  usando `kebab-case` quando houver mais de uma palavra.

## Regras do Acesso60 e do RT03

- Antes de alterar código ou documentação, ler `README.md`,
  `Acesso60-documentacao-RT03-final/README.md`, `Acesso60-documentacao-RT03-final/docs/INDEX.md`
  e os documentos relevantes ao escopo da alteração.
- Separar explicitamente, em documentação e relatórios, **obrigação do RT03**,
  **decisão proposta** e **implementação verificada**.
- Não declarar tecnologia, requisito, conformidade ou resultado de teste como
  fato sem evidência no repositório, no enunciado ou em uma execução registrada.
- Para cada alteração, registrar como validá-la, a evidência esperada e os
  riscos ou pendências que permanecem.
- Ao implementar, atualizar o histórico de desenvolvimento e o status da
  milestone correspondente.
- Considerar, quando aplicável ao escopo, os requisitos RT03 documentados:
  documentação consolidada, protótipo interativo ou Alfa/Beta, relatório de
  uso de IA, evidências de QA, validação de entradas, tratamento de erros sem
  stack traces, testes unitários e roteiro da banca.
- Explicitar o uso de IA, seus riscos e limitações, a intervenção humana e a
  forma de validação dos resultados.
- Usar português claro na documentação, marcar lacunas com `A validar` e
  preferir referências a arquivos, comandos e evidências reproduzíveis.
- Não inventar critérios de aceitação, métricas ou ferramentas ausentes do
  enunciado ou sem evidência no projeto.

## Fluxo mínimo de mudança

Entender o contexto → identificar a obrigação → propor a solução → implementar
→ testar → registrar evidência → atualizar o status.
