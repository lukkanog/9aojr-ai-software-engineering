import { START_LABEL, TIPO_QUESTAO_LABEL, label } from '../utils/labels';
import { useNavigate } from 'react-router-dom';

export default function QuestionList({ questions, isProfessor, examStatus, alreadySubmitted, onAnswerChange, examId }) {
    const navigate = useNavigate();

    return (
        <>
            <h2 className="text-lg font-semibold text-white mb-4">Questões ({questions?.length || 0})</h2>
            <div className="space-y-4 mb-8">
                {questions?.sort((a, b) => a.ordem - b.ordem).map(q => (
                    <div key={q.id} className="bg-slate-800/30 rounded-lg border border-slate-700/30 p-4">
                        <p className="text-white font-medium mb-1">{q.ordem}. {q.enunciado}</p>
                        <p className="text-xs text-slate-500 mb-3">
                            {label(TIPO_QUESTAO_LABEL, q.tipo)} — {q.pontuacao} {q.pontuacao === 1 ? 'ponto' : 'pontos'}
                        </p>
                        <div className="space-y-1">
                            {q.alternativas?.map((alt, i) => (
                                <label key={i} className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer hover:text-white transition">
                                    {!isProfessor && examStatus === 'PUBLICADA' && !alreadySubmitted && (
                                        <input type="radio" name={q.id} value={alt}
                                            onChange={() => onAnswerChange(q.id, alt)}
                                            className="accent-blue-500" />
                                    )}
                                    <span>{alt}</span>
                                </label>
                            ))}
                        </div>
                        {isProfessor && (
                            <button onClick={() => navigate(`/exams/${examId}/questions/${q.id}/issues`)}
                                className="mt-2 text-xs text-yellow-400 hover:text-yellow-300">⚠ Ver Alertas</button>
                        )}
                    </div>
                ))}
            </div>
        </>
    );
}
