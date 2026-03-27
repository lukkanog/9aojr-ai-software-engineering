export const API_STATUS = {
    RASCUNHO: 'RASCUNHO',
    PUBLICADA: 'PUBLICADA',
    ENCERRADA: 'ENCERRADA',
};

export const STATUS_COLORS = {
    [API_STATUS.RASCUNHO]: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
    [API_STATUS.PUBLICADA]: 'bg-green-500/10 text-green-400 border-green-500/20',
    [API_STATUS.ENCERRADA]: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
};
