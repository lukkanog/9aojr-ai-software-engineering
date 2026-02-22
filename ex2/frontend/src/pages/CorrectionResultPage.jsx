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
            .catch(err => setError(err.response?.data?.message || 'Resultado não disponível.'));
    }, [submissionId]);

    if (error) return <div className="max-w-3xl mx-auto p-6"><div className="p-4 rounded-lg bg-red-500/10 text-red-400">{error}</div></div>;
    if (!result) return <div className="text-center py-20 text-slate-400">Carregando...</div>;

    return (
        <div className="max-w-3xl mx-auto p-6">
            <button onClick={() => navigate(-1)} className="text-sm text-slate-400 hover:text-white mb-6">← Voltar</button>
            <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6 mb-6">
                <h1 className="text-2xl font-bold text-white mb-4">Resultado da Correção</h1>
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
                {result.detalhesPorQuestao?.map(d => (
                    <div key={d.questionId} className={`p-4 rounded-lg border ${d.correta ? 'bg-green-500/5 border-green-500/20' : 'bg-red-500/5 border-red-500/20'}`}>
                        <div className="flex justify-between items-center">
                            <span className="text-sm text-slate-300">Questão: {d.questionId.substring(0, 8)}...</span>
                            <span className={`text-xs px-2 py-0.5 rounded-full ${d.correta ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                                {d.correta ? '✓ Correta' : '✗ Incorreta'} — {d.pontuacaoObtida} pts
                            </span>
                        </div>
                        <div className="mt-2 text-xs text-slate-500">
                            Sua resposta: <span className="text-slate-300">{d.respostaAluno || '(em branco)'}</span> | Esperada: <span className="text-slate-300">{d.respostaEsperada}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
