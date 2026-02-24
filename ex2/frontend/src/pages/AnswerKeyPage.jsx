import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as examsApi from '../api/exams';

export default function AnswerKeyPage() {
    const { examId } = useParams();
    const [answerKey, setAnswerKey] = useState(null);
    const [questions, setQuestions] = useState([]);
    const [respostas, setRespostas] = useState({});
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        examsApi.getQuestions(examId).then(res => setQuestions(res.data)).catch(() => { });
        examsApi.getAnswerKey(examId).then(res => {
            setAnswerKey(res.data);
            setRespostas(res.data.respostas || {});
        }).catch(() => { });
    }, [examId]);

    const handleSave = async () => {
        setError(''); setSuccess('');
        try {
            if (answerKey) {
                await examsApi.updateAnswerKey(examId, { respostas });
            } else {
                await examsApi.createAnswerKey(examId, { respostas });
            }
            setSuccess('Gabarito salvo!');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao salvar gabarito.');
        }
    };

    return (
        <div className="max-w-3xl mx-auto p-6">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-white">Gabarito</h1>
                <button onClick={() => navigate(`/exams/${examId}`)} className="text-sm text-slate-400 hover:text-white">← Voltar</button>
            </div>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            {success && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{success}</div>}
            <div className="space-y-4 mb-6">
                {questions.sort((a, b) => a.ordem - b.ordem).map(q => (
                    <div key={q.id} className="p-4 bg-slate-800/30 rounded-lg border border-slate-700/30">
                        <p className="text-white mb-2">{q.ordem}. {q.enunciado}</p>
                        <div className="space-y-1">
                            {q.alternativas?.map((alt, i) => (
                                <label key={i} className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer hover:text-white">
                                    <input type="radio" name={`ak-${q.id}`} value={alt}
                                        checked={respostas[q.id] === alt}
                                        onChange={() => setRespostas(prev => ({ ...prev, [q.id]: alt }))}
                                        className="accent-green-500" />
                                    <span>{alt}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
            <button onClick={handleSave}
                className="w-full py-3 rounded-lg bg-gradient-to-r from-green-600 to-emerald-600 text-white font-semibold hover:from-green-500 hover:to-emerald-500 transition">
                Salvar Gabarito
            </button>
        </div>
    );
}
