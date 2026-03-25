import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as examsApi from '../api/exams';
import * as subsApi from '../api/submissions';
import ExamHeader from '../components/ExamHeader';
import QuestionList from '../components/QuestionList';
import SubmissionsList from '../components/SubmissionsList';

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
            setSuccess('Envio realizado com sucesso!');
            setError('');
            loadExam();
        } catch (err) { setError(err.response?.data?.message || 'Erro ao enviar respostas.'); }
    };

    const handleCorrect = async (subId) => {
        try {
            await subsApi.correctSubmission(subId);
            setSuccess('Envio corrigido!');
            loadExam();
        } catch (err) { setError(err.response?.data?.message || 'Erro ao corrigir.'); }
    };

    if (loading) return (
        <div className="flex items-center justify-center py-20 gap-3 text-slate-400">
            <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            Carregando...
        </div>
    );
    if (!exam) return <div className="text-center py-20 text-red-400">{error || 'Prova não encontrada.'}</div>;

    const alreadySubmitted = !isProfessor && submissions.length > 0;

    return (
        <div className="max-w-4xl mx-auto p-6">
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            {success && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{success}</div>}

            <ExamHeader 
                exam={exam} 
                isProfessor={isProfessor} 
                id={id} 
                onPublish={handlePublish} 
                onClose={handleClose} 
            />

            <QuestionList 
                questions={exam.questions} 
                isProfessor={isProfessor} 
                examStatus={exam.status}
                alreadySubmitted={alreadySubmitted}
                onAnswerChange={(qId, alt) => setAnswers(prev => ({ ...prev, [qId]: alt }))}
                examId={id}
            />

            {!isProfessor && exam.status === 'PUBLICADA' && !alreadySubmitted && (
                <button onClick={handleSubmit}
                    className="w-full py-3 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition">
                    Enviar Respostas
                </button>
            )}
            
            {alreadySubmitted && (
                <div className="p-4 rounded-lg bg-green-500/10 border border-green-500/20 text-green-400">
                    Você já enviou suas respostas.{' '}
                    <button onClick={() => navigate(`/submissions/${submissions[0].id}/result`)} className="underline">Ver resultado</button>
                </div>
            )}

            {isProfessor && <SubmissionsList submissions={submissions} onCorrect={handleCorrect} />}
        </div>
    );
}
