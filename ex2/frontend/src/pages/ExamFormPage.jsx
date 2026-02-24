import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as examsApi from '../api/exams';

export default function ExamFormPage() {
    const { id } = useParams();
    const isEdit = !!id;
    const [titulo, setTitulo] = useState('');
    const [descricao, setDescricao] = useState('');
    const [dataInicio, setDataInicio] = useState('');
    const [dataFim, setDataFim] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        if (isEdit) {
            examsApi.getExam(id).then(res => {
                setTitulo(res.data.titulo);
                setDescricao(res.data.descricao || '');
                if (res.data.dataInicio) setDataInicio(res.data.dataInicio.substring(0, 16));
                if (res.data.dataFim) setDataFim(res.data.dataFim.substring(0, 16));
            }).catch(err => setError(err.response?.data?.message || 'Erro ao carregar.'));
        }
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        const data = {
            titulo,
            descricao: descricao || null,
            dataInicio: dataInicio ? new Date(dataInicio).toISOString() : null,
            dataFim: dataFim ? new Date(dataFim).toISOString() : null,
        };
        try {
            if (isEdit) {
                await examsApi.updateExam(id, data);
            } else {
                await examsApi.createExam(data);
            }
            navigate('/');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao salvar.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-6">
            <h1 className="text-2xl font-bold text-white mb-6">{isEdit ? 'Editar Prova' : 'Nova Prova'}</h1>
            {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                    <label htmlFor="titulo" className="block text-sm font-medium text-slate-300 mb-1">Título</label>
                    <input id="titulo" value={titulo} onChange={e => setTitulo(e.target.value)} required
                        className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
                </div>
                <div>
                    <label htmlFor="descricao" className="block text-sm font-medium text-slate-300 mb-1">Descrição</label>
                    <textarea id="descricao" value={descricao} onChange={e => setDescricao(e.target.value)} rows={3}
                        className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="dataInicio" className="block text-sm font-medium text-slate-300 mb-1">Data Início (opcional)</label>
                        <input id="dataInicio" type="datetime-local" value={dataInicio} onChange={e => setDataInicio(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
                    </div>
                    <div>
                        <label htmlFor="dataFim" className="block text-sm font-medium text-slate-300 mb-1">Data Fim (opcional)</label>
                        <input id="dataFim" type="datetime-local" value={dataFim} onChange={e => setDataFim(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
                    </div>
                </div>
                <div className="flex gap-3">
                    <button type="submit" disabled={loading}
                        className="px-6 py-2.5 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition disabled:opacity-50">
                        {loading ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button type="button" onClick={() => navigate('/')}
                        className="px-6 py-2.5 rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Cancelar</button>
                </div>
            </form>
        </div>
    );
}
