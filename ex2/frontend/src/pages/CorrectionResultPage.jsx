import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as subsApi from '../api/submissions';

export default function CorrectionResultPage() {
    const { submissionId } = useParams();
    const [result, setResult] = useState(null);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        subsApi.getCorrectionResult(submissionId)
            .then(res => setResult(res.data))
            .catch(err => setError(err.response?.data?.message || 'Resultado não disponível ainda.'));
    }, [submissionId]);

    if (error) return (
        <div className="max-w-3xl mx-auto p-6">
            <div className="p-4 rounded-lg bg-red-500/10 text-red-400">{error}</div>
            <button onClick={() => navigate(-1)} className="mt-4 text-sm text-slate-400 hover:text-white">← Voltar</button>
        </div>
    );
    if (!result) return (
        <div className="flex items-center justify-center py-20 gap-3 text-slate-400">
            <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            Carregando resultado...
        </div>
    );

    const total = (result.acertos || 0) + (result.erros || 0);
    const pct = total > 0 ? Math.round((result.acertos / total) * 100) : 0;

    return (
        <div className="max-w-3xl mx-auto p-6">
            <button onClick={() => navigate(-1)} className="text-sm text-slate-400 hover:text-white mb-6">← Voltar</button>
            <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6 mb-6">
                <h1 className="text-2xl font-bold text-white mb-2">Resultado da Correção</h1>

                {/* Barra de desempenho */}
                <div className="mb-6">
                    <div className="flex justify-between text-xs text-slate-400 mb-1">
                        <span>Desempenho geral</span>
                        <span>{pct}%</span>
                    </div>
                    <div className="h-3 bg-slate-700 rounded-full overflow-hidden">
                        <div
                            className={`h-full rounded-full transition-all ${pct >= 70 ? 'bg-gradient-to-r from-green-500 to-emerald-400' : pct >= 50 ? 'bg-gradient-to-r from-yellow-500 to-orange-400' : 'bg-gradient-to-r from-red-600 to-red-400'}`}
                            style={{ width: `${pct}%` }}
                        />
                    </div>
                </div>

                <div className="grid grid-cols-3 gap-4">
                    <div className="text-center p-4 bg-green-500/10 rounded-lg border border-green-500/20">
                        <div className="text-3xl font-bold text-green-400">{result.acertos}</div>
                        <div className="text-sm text-slate-400">Acertos</div>
                    </div>
                    <div className="text-center p-4 bg-red-500/10 rounded-lg border border-red-500/20">
                        <div className="text-3xl font-bold text-red-400">{result.erros}</div>
                        <div className="text-sm text-slate-400">Erros</div>
                    </div>
                    <div className="text-center p-4 bg-blue-500/10 rounded-lg border border-blue-500/20">
                        <div className="text-3xl font-bold text-blue-400">{result.notaFinal}</div>
                        <div className="text-sm text-slate-400">Nota Final</div>
                    </div>
                </div>
            </div>

            <h2 className="text-lg font-semibold text-white mb-4">Detalhes por Questão</h2>
            <div className="space-y-3">
                {result.detalhesPorQuestao?.map((d, idx) => (
                    <div key={d.questionId} className={`p-4 rounded-lg border ${d.correta ? 'bg-green-500/5 border-green-500/20' : 'bg-red-500/5 border-red-500/20'}`}>
                        <div className="flex justify-between items-center">
                            <span className="text-sm text-slate-300 font-medium">Questão {idx + 1}</span>
                            <span className={`text-xs px-2 py-0.5 rounded-full ${d.correta ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                                {d.correta ? '✓ Correta' : '✗ Incorreta'} — {d.pontuacaoObtida} pts
                            </span>
                        </div>
                        <div className="mt-2 text-xs text-slate-500 flex gap-4">
                            <span>Sua resposta: <span className="text-slate-300">{d.respostaAluno || '(em branco)'}</span></span>
                            {!d.correta && <span>Resposta correta: <span className="text-green-400">{d.respostaEsperada}</span></span>}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
