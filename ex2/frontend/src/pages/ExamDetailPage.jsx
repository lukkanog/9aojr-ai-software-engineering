import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as examsApi from '../api/exams';
import * as subsApi from '../api/submissions';

export default function ExamDetailPage() {
    const { id } = useParams();
    const [exam, setExam] = useState(null);
    const [submissions, setSubmissions] = useState([]);
    const [answers, setAnswers] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const { user } = useAuth();
    const navigate = useNavigate();
    const isProfessor = user?.role === 'PROFESSOR';

    useEffect(() => {
        loadExam();
    }, [id]);

    const loadExam = async () => {
        try {
            const res = await examsApi.getExam(id);
            setExam(res.data);
            if (isProfessor || res.data.status !== 'RASCUNHO') {
                try {
                    const subRes = await subsApi.getSubmissions(id);
                    setSubmissions(subRes.data);
                } catch { /* no submissions yet */ }
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao carregar prova.');
        } finally {
            setLoading(false);
        }
    };

    const handlePublish = async () => {
        try {
            const res = await examsApi.publishExam(id);
            setExam(res.data);
            setSuccess('Prova publicada!');
        } catch (err) { setError(err.response?.data?.message || 'Erro ao publicar.'); }
    };

    const handleClose = async () => {
        try {
            const res = await examsApi.closeExam(id);
            setExam(res.data);
            setSuccess('Prova encerrada!');
        } catch (err) { setError(err.response?.data?.message || 'Erro ao encerrar.'); }
    };

    const handleSubmit = async () => {
        try {
            await subsApi.createSubmission(id, { respostas: answers });
            setSuccess('Submissão enviada com sucesso!');
            setError('');
            loadExam();
        } catch (err) { setError(err.response?.data?.message || 'Erro ao enviar submissão.'); }
    };

    const handleCorrect = async (subId) => {
        try {
            await subsApi.correctSubmission(subId);
            setSuccess('Submissão corrigida!');
            loadExam();
        } catch (err) { setError(err.response?.data?.message || 'Erro ao corrigir.'); }
    };

    if (loading) return <div className="text-center py-20 text-slate-400">Carregando...</div>;
    if (!exam) return <div className="text-center py-20 text-red-400">{error || 'Prova não encontrada.'}</div>;

    const alreadySubmitted = !isProfessor && submissions.length > 0;

    return (
        <div className="max-w-4xl mx-auto p-6">
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            {success && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{success}</div>}

            <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6 mb-6">
                <div className="flex justify-between items-start mb-4">
                    <h1 className="text-2xl font-bold text-white">{exam.titulo}</h1>
                    <span className="text-xs px-2 py-1 rounded-full border bg-blue-500/10 text-blue-400 border-blue-500/20">{exam.status}</span>
                </div>
                <p className="text-slate-400 mb-4">{exam.descricao || 'Sem descrição'}</p>

                {isProfessor && (
                    <div className="flex gap-2 flex-wrap">
                        {exam.status === 'RASCUNHO' && (
                            <>
                                <button onClick={() => navigate(`/exams/${id}/edit`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Editar</button>
                                <button onClick={() => navigate(`/exams/${id}/questions`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Questões</button>
                                <button onClick={() => navigate(`/exams/${id}/answer-key`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Gabarito</button>
                                <button onClick={handlePublish} className="px-3 py-1.5 text-sm rounded-lg bg-green-600 text-white hover:bg-green-500 transition">Publicar</button>
                            </>
                        )}
                        {exam.status === 'PUBLICADA' && (
                            <>
                                <button onClick={handleClose} className="px-3 py-1.5 text-sm rounded-lg bg-red-600 text-white hover:bg-red-500 transition">Encerrar</button>
                                <button onClick={() => navigate(`/exams/${id}/answer-key`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Gabarito</button>
                            </>
                        )}
                        <button onClick={() => navigate(`/exams/${id}/report`)} className="px-3 py-1.5 text-sm rounded-lg bg-purple-600/30 text-purple-300 hover:bg-purple-600/50 transition">Relatório</button>
                        <button onClick={() => navigate(`/exams/${id}/statistics`)} className="px-3 py-1.5 text-sm rounded-lg bg-purple-600/30 text-purple-300 hover:bg-purple-600/50 transition">Estatísticas</button>
                    </div>
                )}
            </div>

            {/* Questions */}
            <h2 className="text-lg font-semibold text-white mb-4">Questões ({exam.questions?.length || 0})</h2>
            <div className="space-y-4 mb-8">
                {exam.questions?.sort((a, b) => a.ordem - b.ordem).map(q => (
                    <div key={q.id} className="bg-slate-800/30 rounded-lg border border-slate-700/30 p-4">
                        <p className="text-white font-medium mb-2">{q.ordem}. {q.enunciado}</p>
                        <p className="text-xs text-slate-500 mb-2">{q.tipo} — {q.pontuacao} pts</p>
                        <div className="space-y-1">
                            {q.alternativas?.map((alt, i) => (
                                <label key={i} className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer hover:text-white transition">
                                    {!isProfessor && exam.status === 'PUBLICADA' && !alreadySubmitted && (
                                        <input type="radio" name={q.id} value={alt}
                                            onChange={() => setAnswers(prev => ({ ...prev, [q.id]: alt }))}
                                            className="accent-blue-500" />
                                    )}
                                    <span>{alt}</span>
                                </label>
                            ))}
                        </div>
                        {isProfessor && (
                            <button onClick={() => navigate(`/exams/${id}/questions/${q.id}/issues`)}
                                className="mt-2 text-xs text-yellow-400 hover:text-yellow-300">Ver Issues</button>
                        )}
                    </div>
                ))}
            </div>

            {/* Aluno submit */}
            {!isProfessor && exam.status === 'PUBLICADA' && !alreadySubmitted && (
                <button onClick={handleSubmit}
                    className="w-full py-3 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition">
                    Enviar Respostas
                </button>
            )}
            {alreadySubmitted && (
                <div className="p-4 rounded-lg bg-green-500/10 border border-green-500/20 text-green-400">
                    Você já enviou sua submissão.{' '}
                    <button onClick={() => navigate(`/submissions/${submissions[0].id}/result`)} className="underline">Ver resultado</button>
                </div>
            )}

            {/* Professor submissions list */}
            {isProfessor && submissions.length > 0 && (
                <div className="mt-8">
                    <h2 className="text-lg font-semibold text-white mb-4">Submissões ({submissions.length})</h2>
                    <div className="space-y-2">
                        {submissions.map(sub => (
                            <div key={sub.id} className="flex items-center justify-between p-3 bg-slate-800/30 rounded-lg border border-slate-700/30">
                                <div className="text-sm text-slate-300">
                                    Aluno: {sub.alunoId} — {sub.corrigida ? `Nota: ${sub.nota}` : 'Não corrigida'}
                                </div>
                                {!sub.corrigida && (
                                    <button onClick={() => handleCorrect(sub.id)}
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
            )}
        </div>
    );
}
