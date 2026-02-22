import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as subsApi from '../api/submissions';

const SEV_COLORS = {
    ALTA: 'bg-red-500/10 text-red-400 border-red-500/20',
    MEDIA: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
    BAIXA: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
};

export default function IssuesPage() {
    const { examId, questionId } = useParams();
    const [issues, setIssues] = useState([]);
    const [tipoProblema, setTipoProblema] = useState('');
    const [severidade, setSeveridade] = useState('MEDIA');
    const [descricao, setDescricao] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    useEffect(() => { loadIssues(); }, [questionId]);

    const loadIssues = () => {
        subsApi.getIssues(questionId).then(res => setIssues(res.data)).catch(() => { });
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        setError(''); setSuccess('');
        try {
            await subsApi.createIssue(questionId, { tipoProblema, severidade, descricao });
            setSuccess('Issue registrada!');
            setTipoProblema(''); setDescricao('');
            loadIssues();
        } catch (err) { setError(err.response?.data?.message || 'Erro.'); }
    };

    return (
        <div className="max-w-3xl mx-auto p-6">
            <button onClick={() => navigate(`/exams/${examId}`)} className="text-sm text-slate-400 hover:text-white mb-6">← Voltar</button>
            <h1 className="text-2xl font-bold text-white mb-6">Issues da Questão</h1>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            {success && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{success}</div>}

            <div className="space-y-3 mb-8">
                {issues.length === 0 && <p className="text-slate-500 text-sm">Nenhuma issue registrada.</p>}
                {issues.map(issue => (
                    <div key={issue.id} className={`p-4 rounded-lg border ${SEV_COLORS[issue.severidade]}`}>
                        <div className="flex justify-between items-start mb-1">
                            <span className="font-medium text-sm">{issue.tipoProblema}</span>
                            <span className="text-xs">{issue.geradoPor} | {issue.severidade}</span>
                        </div>
                        <p className="text-xs opacity-80">{issue.descricao}</p>
                    </div>
                ))}
            </div>

            <h2 className="text-lg font-semibold text-white mb-4">Registrar Issue</h2>
            <form onSubmit={handleCreate} className="space-y-4 bg-slate-800/50 p-5 rounded-xl border border-slate-700/50">
                <div>
                    <label htmlFor="tipoProblema" className="block text-sm text-slate-300 mb-1">Tipo do Problema</label>
                    <input id="tipoProblema" value={tipoProblema} onChange={e => setTipoProblema(e.target.value)} required
                        className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none" />
                </div>
                <div>
                    <label htmlFor="sev" className="block text-sm text-slate-300 mb-1">Severidade</label>
                    <select id="sev" value={severidade} onChange={e => setSeveridade(e.target.value)}
                        className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none">
                        <option value="BAIXA">Baixa</option>
                        <option value="MEDIA">Média</option>
                        <option value="ALTA">Alta</option>
                    </select>
                </div>
                <div>
                    <label htmlFor="desc" className="block text-sm text-slate-300 mb-1">Descrição</label>
                    <textarea id="desc" value={descricao} onChange={e => setDescricao(e.target.value)} required rows={3}
                        className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none" />
                </div>
                <button type="submit"
                    className="px-6 py-2 rounded-lg bg-gradient-to-r from-yellow-600 to-orange-600 text-white font-semibold hover:from-yellow-500 hover:to-orange-500 transition">
                    Registrar Issue
                </button>
            </form>
        </div>
    );
}
