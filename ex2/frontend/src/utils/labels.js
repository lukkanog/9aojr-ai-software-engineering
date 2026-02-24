// Mapas de tradução para enums do backend → textos user-friendly

export const STATUS_LABEL = {
    RASCUNHO: 'Rascunho',
    PUBLICADA: 'Publicada',
    ENCERRADA: 'Encerrada',
};

export const TIPO_QUESTAO_LABEL = {
    OBJETIVA: 'Objetiva',
    VERDADEIRO_FALSO: 'Verdadeiro / Falso',
    DISSERTATIVA: 'Dissertativa',
};

export const SEVERIDADE_LABEL = {
    ALTA: 'Alta',
    MEDIA: 'Média',
    BAIXA: 'Baixa',
};

export const ROLE_LABEL = {
    PROFESSOR: 'Professor',
    ALUNO: 'Aluno',
};

export const GERADO_POR_LABEL = {
    MANUAL: 'Manual',
    AUTOMATICO: 'Automático',
};

/** Retorna o label amigável ou o valor original caso não mapeado */
export const label = (map, value) => map[value] ?? value;
