import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as subsApi from '../api/submissions';

export default function ReportPage() {
    const { examId } = useParams();
    const [report, setReport] = useState(null);
    const [stats, setStats] = useState(null);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        subsApi.getReport(examId).then(res => setReport(res.data)).catch(err => setError(err.response?.data?.message || 'Erro.'));
        subsApi.getStatistics(examId).then(res => setStats(res.data)).catch(() => { });
    }, [examId]);

    return (
        <div className="max-w-4xl mx-auto p-6">
            <button onClick={() => navigate(`/exams/${examId}`)} className="text-sm text-slate-400 hover:text-white mb-6">← Voltar</button>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}

            {report && (
                <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6 mb-6">
                    <h1 className="text-2xl font-bold text-white mb-4">Relatório</h1>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="text-center p-4 bg-blue-500/10 rounded-lg border border-blue-500/20">
                            <div className="text-2xl font-bold text-blue-400">{report.mediaNotas}</div>
                            <div className="text-xs text-slate-400">Média</div>
                        </div>
                        <div className="text-center p-4 bg-green-500/10 rounded-lg border border-green-500/20">
                            <div className="text-2xl font-bold text-green-400">{report.maiorNota}</div>
                            <div className="text-xs text-slate-400">Maior Nota</div>
                        </div>
                        <div className="text-center p-4 bg-red-500/10 rounded-lg border border-red-500/20">
                            <div className="text-2xl font-bold text-red-400">{report.menorNota}</div>
                            <div className="text-xs text-slate-400">Menor Nota</div>
                        </div>
                        <div className="text-center p-4 bg-purple-500/10 rounded-lg border border-purple-500/20">
                            <div className="text-2xl font-bold text-purple-400">{report.totalSubmissoes}</div>
                            <div className="text-xs text-slate-400">Submissões</div>
                        </div>
                    </div>
                </div>
            )}

            {stats && (
                <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6">
                    <h2 className="text-xl font-bold text-white mb-4">Estatísticas</h2>
                    <h3 className="text-sm font-semibold text-slate-300 mb-2">Acerto por Questão</h3>
                    <div className="space-y-2 mb-6">
                        {Object.entries(stats.percentualAcertoPorQuestao || {}).map(([qId, pct]) => (
                            <div key={qId} className="flex items-center gap-3">
                                <span className="text-xs text-slate-400 w-24 truncate">{qId.substring(0, 8)}...</span>
                                <div className="flex-1 h-4 bg-slate-700 rounded-full overflow-hidden">
                                    <div className="h-full bg-gradient-to-r from-blue-500 to-purple-500 rounded-full transition-all" style={{ width: `${pct}%` }} />
                                </div>
                                <span className="text-xs text-slate-300 w-12 text-right">{pct}%</span>
                            </div>
                        ))}
                    </div>
                    <h3 className="text-sm font-semibold text-slate-300 mb-2">Distribuição de Notas</h3>
                    <div className="grid grid-cols-5 gap-2">
                        {Object.entries(stats.distribuicaoNotas || {}).map(([faixa, count]) => (
                            <div key={faixa} className="text-center p-2 bg-slate-700/50 rounded-lg">
                                <div className="text-sm font-bold text-white">{count}</div>
                                <div className="text-xs text-slate-400">{faixa}</div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
