import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuestions, useAnswerKey, useSaveAnswerKey } from '../hooks/useExams';

export default function AnswerKeyPage() {
    const { examId } = useParams();
    const navigate = useNavigate();

    const [respostas, setRespostas] = useState({});
    const [localError, setLocalError] = useState('');
    const [success, setSuccess] = useState('');

    const { data: questions = [], isLoading: isLoadingQuestions } = useQuestions(examId);
    const { data: answerKey, isLoading: isLoadingAnswerKey } = useAnswerKey(examId);
    const saveMutation = useSaveAnswerKey();

    useEffect(() => {
        if (answerKey?.respostas) {
            setRespostas(answerKey.respostas);
        }
    }, [answerKey]);

    const handleSave = async () => {
        setLocalError(''); 
        setSuccess('');
        try {
            await saveMutation.mutateAsync({
                examId,
                data: { respostas },
                isUpdate: !!answerKey
            });
            setSuccess('Gabarito salvo com sucesso!');
        } catch (err) {
            setLocalError(err.response?.data?.message || 'Erro ao salvar gabarito.');
        }
    };

    if (isLoadingQuestions || isLoadingAnswerKey) {
        return <div className="text-center py-20 text-slate-400">Carregando dados da prova...</div>;
    }

    return (
        <div className="max-w-3xl mx-auto p-6">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-white">Gabarito</h1>
                <button onClick={() => navigate(`/exams/${examId}`)} className="text-sm text-slate-400 hover:text-white">← Voltar</button>
            </div>
            
            {localError && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{localError}</div>}
            {success && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{success}</div>}
            
            <div className="space-y-4 mb-6">
                {[...questions].sort((a, b) => a.ordem - b.ordem).map(q => (
                    <div key={q.id} className="p-4 bg-slate-800/30 rounded-lg border border-slate-700/30">
                        <p className="text-white mb-3 font-medium">{q.ordem}. {q.enunciado}</p>
                        <div className="space-y-2">
                            {q.alternativas?.map((alt, i) => (
                                <label key={i} className="flex items-start gap-3 text-sm text-slate-300 cursor-pointer hover:text-white p-2 rounded-md hover:bg-slate-700/30 transition">
                                    <input type="radio" name={`ak-${q.id}`} value={alt}
                                        checked={respostas[q.id] === alt}
                                        onChange={() => setRespostas(prev => ({ ...prev, [q.id]: alt }))}
                                        className="mt-0.5 accent-green-500 w-4 h-4" />
                                    <span>{alt}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
            
            <button onClick={handleSave} disabled={saveMutation.isPending}
                className="w-full py-3 rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 text-white font-semibold hover:from-green-500 hover:to-emerald-500 transition disabled:opacity-50 disabled:cursor-not-allowed">
                {saveMutation.isPending ? 'Salvando...' : 'Salvar Gabarito'}
            </button>
        </div>
    );
}
