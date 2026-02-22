import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as examsApi from '../api/exams';

const STATUS_COLORS = {
    RASCUNHO: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
    PUBLICADA: 'bg-green-500/10 text-green-400 border-green-500/20',
    ENCERRADA: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
};

export default function ExamListPage() {
    const [exams, setExams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const { user } = useAuth();
    const navigate = useNavigate();
    const isProfessor = user?.role === 'PROFESSOR';

    useEffect(() => {
        examsApi.getExams()
            .then(res => setExams(res.data))
            .catch(err => setError(err.response?.data?.message || 'Erro ao carregar provas.'))
            .finally(() => setLoading(false));
    }, []);

    const handleDelete = async (id) => {
        if (!confirm('Tem certeza que deseja excluir esta prova?')) return;
        try {
            await examsApi.deleteExam(id);
            setExams(exams.filter(e => e.id !== id));
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao excluir.');
        }
    };

    if (loading) return <div className="text-center py-20 text-slate-400">Carregando...</div>;

    return (
        <div className="max-w-6xl mx-auto p-6">
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-2xl font-bold text-white">Provas</h1>
                {isProfessor && (
                    <button onClick={() => navigate('/exams/new')}
                        className="px-4 py-2 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white text-sm font-semibold hover:from-blue-500 hover:to-purple-500 transition">
                        + Nova Prova
                    </button>
                )}
            </div>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            {exams.length === 0 ? (
                <div className="text-center py-16 text-slate-500">Nenhuma prova encontrada.</div>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {exams.map(exam => (
                        <Link key={exam.id} to={`/exams/${exam.id}`}
                            className="p-5 rounded-xl bg-slate-800/50 border border-slate-700/50 hover:border-blue-500/30 hover:bg-slate-800/80 transition group">
                            <div className="flex items-start justify-between mb-3">
                                <h2 className="text-lg font-semibold text-white group-hover:text-blue-400 transition">{exam.titulo}</h2>
                                <span className={`text-xs px-2 py-1 rounded-full border ${STATUS_COLORS[exam.status]}`}>{exam.status}</span>
                            </div>
                            <p className="text-sm text-slate-400 mb-3 line-clamp-2">{exam.descricao || 'Sem descrição'}</p>
                            <div className="text-xs text-slate-500">{exam.questions?.length || 0} questões</div>
                            {isProfessor && exam.status === 'RASCUNHO' && (
                                <button onClick={(e) => { e.preventDefault(); handleDelete(exam.id); }}
                                    className="mt-3 text-xs text-red-400 hover:text-red-300">Excluir</button>
                            )}
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
