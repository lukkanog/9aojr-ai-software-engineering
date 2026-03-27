import { useNavigate } from 'react-router-dom';

export default function SubmissionsList({ submissions, onCorrect }) {
    const navigate = useNavigate();

    if (!submissions || submissions.length === 0) return null;

    return (
        <div className="mt-8">
            <h2 className="text-lg font-semibold text-white mb-4">Envios ({submissions.length})</h2>
            <div className="space-y-2">
                {submissions.map((sub, idx) => (
                    <div key={sub.id} className="flex items-center justify-between p-3 bg-slate-800/30 rounded-lg border border-slate-700/30">
                        <div className="text-sm text-slate-300">
                            <span className="font-medium text-white">
                                {sub.alunoNome || sub.alunoEmail || `Aluno #${idx + 1}`}
                            </span>
                            {' — '}
                            {sub.corrigida
                                ? <span className="text-green-400">Nota: {sub.nota}</span>
                                : <span className="text-yellow-400">Aguardando correção</span>
                            }
                        </div>
                        {!sub.corrigida && (
                            <button onClick={() => onCorrect(sub.id)}
                                className="text-xs px-3 py-1 rounded bg-blue-600 text-white hover:bg-blue-500 transition">Corrigir</button>
                        )}
                        {sub.corrigida && (
                            <button onClick={() => navigate(`/submissions/${sub.id}/result`)}
                                className="text-xs px-3 py-1 rounded bg-slate-600 text-white hover:bg-slate-500 transition">Ver Resultado</button>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}
