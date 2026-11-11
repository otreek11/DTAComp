/**
 * Dados de exemplo (texto puro) da tela de revisão, até a API existir.
 * Formato pensado para ser o mesmo que o backend devolverá:
 *   trabalho: { id, tipo, titulo, autor, instituicao, data, reenviado?, blocks, sugestoes,
 *               statusInicial?, decisoesIniciais?, parecerInicial? }
 *   blocks:   formato do ArticleBody; blocos "p" podem ter marks: [{ inicio, fim, sugestaoId }]
 *   sugestao: { id, categoria, trecho }  (trecho = descrição exibida no card)
 */

/** Cria um bloco "p" calculando os marks a partir dos trechos literais: [literal, sugestaoId]. */
function paragrafo(text, ...ancoras) {
  const marks = ancoras
    .map(([literal, sugestaoId]) => {
      const inicio = text.indexOf(literal);
      return inicio < 0 ? null : { inicio, fim: inicio + literal.length, sugestaoId };
    })
    .filter(Boolean);
  return { type: "p", text, marks };
}

const ORTOGRAFIA = "Trecho com possível erro e sugestão de correção.";
const LINGUAGEM = "Trecho que pode ferir as diretrizes de conteúdo.";

export const TRABALHOS_MOCK = [
  {
    id: "t1",
    tipo: "texto",
    titulo: "Irrigação por gotejamento e a produtividade do feijão-caupi na baixada maranhense",
    data: "08 nov 2026",
    blocks: [
      paragrafo(
        "Esta pesquisa avaliou o efeito da irrigasão por gotejamento em lavouras de feijão-caupi na baixada maranhense, comparando áreas irrigadas e de sequeiro durante duas safras consecutivas.",
        ["irrigasão", "t1-s1"],
      ),
      paragrafo(
        "Os dados coletados mostra um ganho expressivo de produtividade nas áreas irrigadas, embora o custo de implantação continue sendo um fator limitante para os pequenos produtores da região.",
        ["Os dados coletados mostra", "t1-s2"],
      ),
      paragrafo(
        "Por fim, os agricultores que ainda não adotam a técnica são preguiçosos e atrasados, e por isso permanecem dependentes das chuvas.",
        ["são preguiçosos e atrasados", "t1-s3"],
      ),
    ],
    sugestoes: [
      { id: "t1-s1", categoria: "Ortografia", trecho: ORTOGRAFIA },
      { id: "t1-s2", categoria: "Concordância", trecho: "Trecho com possível erro de concordância verbal." },
      { id: "t1-s3", categoria: "Linguagem inadequada", trecho: LINGUAGEM },
    ],
  },
  {
    id: "t2",
    tipo: "pdf",
    titulo: "Mapeamento de áreas de risco de alagamento em São Luís",
    arquivoURL: "/exemplos/revisao_exemplo.pdf",
    data: "05 nov 2026",
    blocks: [],
    sugestoes: [],
  },
  {
    id: "t3",
    tipo: "texto",
    titulo: "Redes de sensores de baixo custo para monitoramento da qualidade do ar",
    data: "03 nov 2026",
    reenviado: true,
    blocks: [
      paragrafo(
        "Este trabalho descreve a montagem de uma rede de sensores de baixo custo para medir material particulado em áreas urbanas, com foco na reprodutibilidade por equipes de pequeno porte.",
      ),
      paragrafo(
        "Os resultados preliminares indicam boa correlação com estações de referência, mas a calibração em campo ainda exige cuidados com umidade e temperatura. A análise estatística foi refeita conforme o parecer anterior, e o conjunto de dados passou a incluir as medições de dezembro.",
        ["conjunto de dados passou a incluir", "t3-s1"],
      ),
    ],
    sugestoes: [
      { id: "t3-s1", categoria: "Clareza", trecho: "Trecho com construção ambígua; considere reescrever." },
    ],
  },
  {
    id: "t4",
    tipo: "texto",
    titulo: "Introdução à análise de dados com Python para estudantes de graduação",
    data: "28 out 2026",
    statusInicial: "aprovado",
    blocks: [
      paragrafo(
        "Este texto apresenta, de forma gradual, as bibliotecas mais usadas para análise de dados em Python, com exemplos pensados para a rotina de quem está começando a pesquisa.",
      ),
    ],
    sugestoes: [
      { id: "t4-s1", categoria: "Ortografia", trecho: ORTOGRAFIA },
    ],
    decisoesIniciais: { "t4-s1": "ignorada" },
    parecerInicial: "",
  },
  {
    id: "t5",
    tipo: "texto",
    titulo: "Perspectivas sobre o ensino remoto em escolas públicas do interior",
    data: "22 out 2026",
    statusInicial: "devolvido",
    blocks: [
      paragrafo(
        "A pesquisa reuniu relatos de professores sobre o ensino remoto durante a pandemia, mas as conclusões generalizam resultados de apenas duas escolas.",
        ["generalizam resultados de apenas duas escolas", "t5-s1"],
      ),
    ],
    sugestoes: [
      { id: "t5-s1", categoria: "Clareza", trecho: "Afirmação ampla demais para a amostra descrita." },
    ],
    decisoesIniciais: { "t5-s1": "aceita" },
    parecerInicial: "As conclusões precisam ser limitadas à amostra de duas escolas. Revise também a seção de metodologia.",
  },
];
