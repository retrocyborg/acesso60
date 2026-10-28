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
- `core/`: módulo compartilhado Kotlin/JVM, sem dependências Android.
- `app/`: módulo Android da aplicação e suas integrações de plataforma.
- `docs/`: documentação do projeto e decisões técnicas.
- `scripts/`: scripts de desenvolvimento, manutenção e automação.
- `config/`: arquivos de configuração não sensíveis.
- `assets/`: recursos estáticos usados pela aplicação.
- `artifacts/`: artefatos de entrega gerados, como APKs para instalação e demonstração; binários permanecem fora do versionamento.
- `gradle/`: binários e configurações do Gradle Wrapper.

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
- Manter o domínio de comandos, validação, risco e resultados no núcleo
  compartilhado `acesso60-core`. O módulo Android deve consumi-lo como
  dependência e limitar-se a adaptadores, UI e integrações Android.
- Não criar uma segunda implementação de `Command`, `CommandInterpreter`,
  `CommandValidator` ou política de risco no Android. Qualquer exceção deve
  ser registrada em `docs/project/architecture.md` e justificada por um
  contrato de adaptação testado.
- Não mover ou remover arquivos existentes sem verificar seu uso e registrar a
  mudança.
- Nunca gravar segredos em arquivos versionados.
- Manter nomes de pastas e arquivos claros, consistentes e em minúsculas,
  usando `kebab-case` quando houver mais de uma palavra.

## Regras do Acesso60 e do RT03

- Antes de alterar código ou documentação, ler `README.md`,
  `docs/INDEX.md`, `docs/milestones/roadmap.md` e os documentos relevantes ao escopo da alteração.
- Consultar e seguir o `docs/milestones/roadmap.md` para a priorização e
  sequenciamento das atividades (P0, P1, P2). Nenhuma tarefa do roadmap deve
  ser marcada como concluída apenas por planejamento ou existência de código,
  exigindo execução comprovada e registro formal no padrão de evidências
  (data, commit, comando, resultado observado e pendências remanescentes).
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
- Adotar como padrão-ouro metodológico para testes e qualidade de software as
  normas ISO/IEC/IEEE 29119/29119-1 (processos de teste), IEEE 829 (documentação
  de testes), IEEE 1012 (verificação e validação), IEEE 1028 (revisões de software),
  IEEE 1044 (classificação de anomalias) e os princípios da ISO 9000:2026 (gestão
  da qualidade baseada em processos e evidências).

## Fluxo mínimo de mudança

Entender o contexto → identificar a obrigação → propor a solução → implementar
→ testar → registrar evidência → atualizar o status.

## Instruções personalizadas de resposta

As orientações abaixo incorporam `minhas_custom_instructions.md` e devem ser
aplicadas de forma compatível com as regras do repositório:

- Citar somente informações explicitamente presentes no material recebido ou
  verificadas durante a execução. O que não puder ser verificado deve ser
  marcado como **não verificável** ou **A validar**, conforme o contexto.
- Tratar a primeira resposta como um esboço sujeito a refinamento, quando a
  natureza da tarefa permitir iteração.
- Sugerir assuntos correlatos que ampliem o conhecimento e ajudem a avaliar o
  contexto da tarefa.
- Quando houver fundamentação externa, priorizar livros, artigos, papers e
  outras referências bibliográficas confiáveis. Parafrasear as fontes e
  apresentar as referências ao final.
- Usar termos técnicos e tom acadêmico quando forem adequados ao assunto,
  mantendo clareza e evitando complexidade sem utilidade.
- Apresentar análise crítica, perspectivas e limitações das fontes ou das
  decisões propostas, sem apresentar inferências como fatos.
- Estruturar respostas extensas em tópicos aprofundáveis quando a tarefa
  exigir estudo ou documentação detalhada. Para tarefas simples, manter a
  resposta proporcional ao objetivo.
- Quando a atualidade das informações for relevante, realizar pesquisa em
  fontes adequadas antes de afirmar que algo é atual. Registrar as fontes
  utilizadas e distinguir evidência externa de evidência do repositório.

Estas preferências não autorizam inventar requisitos, métricas, resultados ou
referências. Em caso de conflito, prevalecem o enunciado da tarefa, as regras
do `Agents.md` e as instruções superiores aplicáveis.
