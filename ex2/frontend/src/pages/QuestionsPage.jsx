import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as examsApi from '../api/exams';
import { TIPO_QUESTAO_LABEL, label } from '../utils/labels';

export default function QuestionsPage() {
    const { examId } = useParams();
    const [questions, setQuestions] = useState([]);
    const [enunciado, setEnunciado] = useState('');
    const [tipo, setTipo] = useState('OBJETIVA');
    const [alternativas, setAlternativas] = useState(['', '']);
    const [pontuacao, setPontuacao] = useState(1);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => { loadQuestions(); }, [examId]);

    const loadQuestions = async () => {
        try {
            const res = await examsApi.getQuestions(examId);
            setQuestions(res.data);
        } catch (err) { setError(err.response?.data?.message || 'Erro.'); }
    };

    const handleAdd = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await examsApi.addQuestion(examId, {
                enunciado, tipo, alternativas: alternativas.filter(a => a.trim()),
                pontuacao, ordem: questions.length + 1
            });
            setEnunciado(''); setAlternativas(['', '']); setPontuacao(1);
            loadQuestions();
        } catch (err) { setError(err.response?.data?.message || 'Erro ao adicionar.'); }
    };

    const handleDelete = async (qId) => {
        if (!confirm('Deseja remover esta questão?')) return;
        try { await examsApi.deleteQuestion(qId); loadQuestions(); }
        catch (err) { setError(err.response?.data?.message || 'Erro.'); }
    };

    return (
        <div className="max-w-3xl mx-auto p-6">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-white">Questões</h1>
                <button onClick={() => navigate(`/exams/${examId}`)} className="text-sm text-slate-400 hover:text-white">← Voltar</button>
            </div>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}

            {/* Questões existentes */}
            <div className="space-y-3 mb-8">
                {questions.length === 0 && (
                    <p className="text-slate-500 text-sm text-center py-6">Nenhuma questão adicionada ainda.</p>
                )}
                {questions.sort((a, b) => a.ordem - b.ordem).map(q => (
                    <div key={q.id} className="p-4 bg-slate-800/30 rounded-lg border border-slate-700/30 flex justify-between items-start gap-4">
                        <div className="flex-1 min-w-0">
                            <p className="text-white font-medium">{q.ordem}. {q.enunciado}</p>
                            <p className="text-xs text-slate-500 mt-1">
                                {label(TIPO_QUESTAO_LABEL, q.tipo)} · {q.pontuacao} {q.pontuacao === 1 ? 'ponto' : 'pontos'}
                            </p>
                            {q.alternativas?.length > 0 && (
                                <p className="text-xs text-slate-600 mt-1">
                                    Alternativas: {q.alternativas.join(' / ')}
                                </p>
                            )}
                        </div>
                        <button onClick={() => handleDelete(q.id)} className="text-red-400 text-xs hover:text-red-300 flex-shrink-0">Excluir</button>
                    </div>
                ))}
            </div>

            {/* Formulário de nova questão */}
            <h2 className="text-lg font-semibold text-white mb-4">Adicionar Questão</h2>
            <form onSubmit={handleAdd} className="space-y-4 bg-slate-800/50 p-5 rounded-xl border border-slate-700/50">
                <div>
                    <label htmlFor="enunciado" className="block text-sm text-slate-300 mb-1">Enunciado</label>
                    <textarea id="enunciado" value={enunciado} onChange={e => setEnunciado(e.target.value)} required rows={2}
                        className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="tipo" className="block text-sm text-slate-300 mb-1">Tipo</label>
                        <select id="tipo" value={tipo} onChange={e => setTipo(e.target.value)}
                            className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none">
                            <option value="OBJETIVA">Objetiva</option>
                            <option value="VERDADEIRO_FALSO">Verdadeiro / Falso</option>
                        </select>
                    </div>
                    <div>
                        <label htmlFor="pontuacao" className="block text-sm text-slate-300 mb-1">Pontuação</label>
                        <input id="pontuacao" type="number" min={0.1} step={0.1} value={pontuacao} onChange={e => setPontuacao(Number(e.target.value))}
                            className="w-full px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none" />
                    </div>
                </div>
                <div>
                    <label className="block text-sm text-slate-300 mb-1">Alternativas</label>
                    {alternativas.map((alt, i) => (
                        <div key={i} className="flex gap-2 mb-2">
                            <input value={alt} onChange={e => { const n = [...alternativas]; n[i] = e.target.value; setAlternativas(n); }}
                                placeholder={`Alternativa ${i + 1}`}
                                className="flex-1 px-4 py-2 rounded-lg bg-slate-700/50 border border-slate-600 text-white outline-none" />
                            {alternativas.length > 2 && (
                                <button type="button" onClick={() => setAlternativas(alternativas.filter((_, j) => j !== i))}
                                    className="text-red-400 text-sm px-2" title="Remover alternativa">✕</button>
                            )}
                        </div>
                    ))}
                    <button type="button" onClick={() => setAlternativas([...alternativas, ''])}
                        className="text-xs text-blue-400 hover:text-blue-300">+ Adicionar alternativa</button>
                </div>
                <button type="submit"
                    className="px-6 py-2 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition">
                    Adicionar Questão
                </button>
            </form>
        </div>
    );
}
