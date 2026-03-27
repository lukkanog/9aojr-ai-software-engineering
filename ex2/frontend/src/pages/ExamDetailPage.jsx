import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useExam, usePublishExam, useCloseExam } from '../hooks/useExams';
import { useSubmissions, useCreateSubmission, useCorrectSubmission } from '../hooks/useSubmissions';
import ExamHeader from '../components/ExamHeader';
import QuestionList from '../components/QuestionList';
import SubmissionsList from '../components/SubmissionsList';

export default function ExamDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const isProfessor = user?.role === 'PROFESSOR';

    const [answers, setAnswers] = useState({});
    const [success, setSuccess] = useState('');
    const [localError, setLocalError] = useState('');

    const { data: exam, isLoading: isLoadingExam, isError: isExamError, error: examError } = useExam(id);
    
    // Only fetch submissions if user is professor or exam is not a draft (already handled by enabled flag implicitly via API, but we can just fetch it always for this page)
    const { data: submissions = [] } = useSubmissions(id);

    const publishMutation = usePublishExam();
    const closeMutation = useCloseExam();
    const submitMutation = useCreateSubmission();
    const correctMutation = useCorrectSubmission();

    const handlePublish = async () => {
        try {
            await publishMutation.mutateAsync(id);
            setSuccess('Prova publicada!');
            setLocalError('');
        } catch (err) { setLocalError(err.response?.data?.message || 'Erro ao publicar.'); }
    };

    const handleClose = async () => {
        try {
            await closeMutation.mutateAsync(id);
            setSuccess('Prova encerrada!');
            setLocalError('');
        } catch (err) { setLocalError(err.response?.data?.message || 'Erro ao encerrar.'); }
    };

    const handleSubmit = async () => {
        try {
            await submitMutation.mutateAsync({ examId: id, data: { respostas: answers } });
            setSuccess('Envio realizado com sucesso!');
            setLocalError('');
        } catch (err) { setLocalError(err.response?.data?.message || 'Erro ao enviar respostas.'); }
    };

    const handleCorrect = async (subId) => {
        try {
            await correctMutation.mutateAsync(subId);
            setSuccess('Envio corrigido!');
            setLocalError('');
        } catch (err) { setLocalError(err.response?.data?.message || 'Erro ao corrigir.'); }
    };

    if (isLoadingExam) return (
        <div className="flex items-center justify-center py-20 gap-3 text-slate-400">
            <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
            </svg>
            Carregando...
        </div>
    );

    if (isExamError || !exam) {
        return <div className="text-center py-20 text-red-400">{examError?.response?.data?.message || 'Prova não encontrada.'}</div>;
    }

    const alreadySubmitted = !isProfessor && submissions.length > 0;
    const currentError = localError || submitMutation.error?.response?.data?.message;

    return (
        <div className="max-w-4xl mx-auto p-6">
            {currentError && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{currentError}</div>}
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
                <button onClick={handleSubmit} disabled={submitMutation.isPending}
                    className="w-full py-3 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition disabled:opacity-50">
                    {submitMutation.isPending ? 'Enviando...' : 'Enviar Respostas'}
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
