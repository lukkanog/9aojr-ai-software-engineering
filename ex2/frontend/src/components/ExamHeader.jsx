import { STATUS_LABEL, label } from '../utils/labels';
import { useNavigate } from 'react-router-dom';

const STATUS_COLORS = {
    RASCUNHO: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
    PUBLICADA: 'bg-green-500/10 text-green-400 border-green-500/20',
    ENCERRADA: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
};

export default function ExamHeader({ exam, isProfessor, id, onPublish, onClose }) {
    const navigate = useNavigate();

    return (
        <div className="bg-slate-800/50 rounded-xl border border-slate-700/50 p-6 mb-6">
            <div className="flex justify-between items-start mb-4">
                <h1 className="text-2xl font-bold text-white">{exam.titulo}</h1>
                <span className={`text-xs px-2 py-1 rounded-full border ${STATUS_COLORS[exam.status]}`}>
                    {label(STATUS_LABEL, exam.status)}
                </span>
            </div>
            <p className="text-slate-400 mb-4">{exam.descricao || 'Sem descrição'}</p>

            {isProfessor && (
                <div className="flex gap-2 flex-wrap">
                    {exam.status === 'RASCUNHO' && (
                        <>
                            <button onClick={() => navigate(`/exams/${id}/edit`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Editar</button>
                            <button onClick={() => navigate(`/exams/${id}/questions`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Questões</button>
                            <button onClick={() => navigate(`/exams/${id}/answer-key`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Gabarito</button>
                            <button onClick={onPublish} className="px-3 py-1.5 text-sm rounded-lg bg-green-600 text-white hover:bg-green-500 transition">Publicar</button>
                        </>
                    )}
                    {exam.status === 'PUBLICADA' && (
                        <>
                            <button onClick={onClose} className="px-3 py-1.5 text-sm rounded-lg bg-red-600 text-white hover:bg-red-500 transition">Encerrar</button>
                            <button onClick={() => navigate(`/exams/${id}/answer-key`)} className="px-3 py-1.5 text-sm rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Gabarito</button>
                        </>
                    )}
                    <button onClick={() => navigate(`/exams/${id}/report`)} className="px-3 py-1.5 text-sm rounded-lg bg-purple-600/30 text-purple-300 hover:bg-purple-600/50 transition">Relatório</button>
                    <button onClick={() => navigate(`/exams/${id}/statistics`)} className="px-3 py-1.5 text-sm rounded-lg bg-purple-600/30 text-purple-300 hover:bg-purple-600/50 transition">Estatísticas</button>
                </div>
            )}
        </div>
    );
}
