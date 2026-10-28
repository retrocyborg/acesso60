# Plano de Implementação — Padrões-Ouro de Testes e Qualidade de Software

## Objetivo
Incorporar formalmente as normas e padrões internacionais de engenharia de software e testes como referencial normativo (padrão-ouro) do projeto Acesso60:
- **Testes e V&V:**
  - **ISO/IEC/IEEE 29119 / 29119-1:** Conceitos, vocabulário e processos de teste de software.
  - **IEEE 829:** Padrão para documentação de testes de sistemas e software.
  - **IEEE 1012:** Verificação e Validação (V&V) independente de software e hardware.
  - **IEEE 1028:** Revisões técnicas, auditorias e inspeções formais de software.
  - **IEEE 1044:** Classificação padronizada de anomalias e defeitos de software.
- **Gestão da Qualidade de Software:**
  - **ISO 9000 / revisão 2026:** Fundamentos e vocabulário de sistemas de gestão da qualidade, com foco em melhoria contínua, satisfação do cliente e conformidade de processo.

---

## User Review Required

> [!IMPORTANT]
> **Enquadramento Acadêmico e Prático**: As normas serão adotadas de forma proporcional à escala do projeto MVP (adaptando processos para desenvolvimento ágil assistido por IA sem gerar burocracia excessiva ou documentação ociosa, em conformidade com o princípio KISS/YAGNI de [`docs/standards/software-principles.md`](file:///C:/Prog/acesso60/docs/standards/software-principles.md)).

---

## Proposed Changes

### Padrões e Normas (`docs/standards/`)

#### [MODIFY] `qa-rules.md`
- Adicionar a seção **Normas e Padrões de Referência (Padrão-Ouro)**:
  - Mapear os 6 critérios acadêmicos (`ACAD-Q1` a `ACAD-Q6`) às normas internacionais correspondentes (ISO 9000/2026 para gestão e processos; IEEE 29119 e IEEE 1012 para V&V).
  - Alinhar a política de anomalias/defeitos com as diretrizes do **IEEE 1044** (severidade, impacto no usuário idoso, reprodutibilidade).
  - Definir a prática de revisões formais de código e documentação segundo o **IEEE 1028**.

#### [MODIFY] `quality-attributes.md`
- Adicionar referencial de qualidade de processos baseado nos princípios de gestão da **ISO 9000/2026** (foco no usuário idoso, liderança/propriedade, abordagem por processos, melhoria contínua e tomada de decisão baseada em evidências).

---

### Engenharia e Testes (`docs/engineering/`)

#### [MODIFY] `testing-strategy.md`
- Adicionar a fundamentação normativa formal na estratégia de testes:
  - Adotar a terminologia e processos fundamentais da **ISO/IEC/IEEE 29119-1**.
  - Estruturar os relatórios e especificações de teste em conformidade com a estrutura documental do **IEEE 829** (identificação do teste, pré-condições, procedimento, resultado esperado vs. observado).
  - Incorporar a matriz de níveis de integridade e rigor de V&V do **IEEE 1012** (garantindo que ações sensíveis e políticas de risco tenham nível máximo de rigor de verificação).

---

### Diretrizes de Governança (`Agents.md`)

#### [MODIFY] `Agents.md`
- Inserir na seção de **Regras do Acesso60 e do RT03** a menção explícita à observância dos padrões-ouro (IEEE 29119, IEEE 829, IEEE 1012, IEEE 1028, IEEE 1044 e ISO 9000/2026) na estruturação de relatórios, casos de teste e registros de defeitos.

---

## Verification Plan

### Manual Verification
1. Verificar a consistência cruzada entre `qa-rules.md`, `testing-strategy.md` e `Agents.md`.
2. Confirmar que a taxonomia de evidências usada em `BETA_NUCLEO_TODO.md` e `docs/milestones/roadmap.md` reflete os atributos fundamentais do IEEE 829 / IEEE 1044 (data, ambiente, comando, resultado esperado vs. observado, severidade de pendências).
