import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useExam, useCreateExam, useUpdateExam } from '../hooks/useExams';

export default function ExamFormPage() {
    const { id } = useParams();
    const isEdit = !!id;
    const navigate = useNavigate();

    const [titulo, setTitulo] = useState('');
    const [descricao, setDescricao] = useState('');
    const [dataInicio, setDataInicio] = useState('');
    const [dataFim, setDataFim] = useState('');

    const { data: examData, isLoading: isLoadingExam, isError: isExamError } = useExam(isEdit ? id : null);
    const createMutation = useCreateExam();
    const updateMutation = useUpdateExam();

    useEffect(() => {
        if (isEdit && examData) {
            setTitulo(examData.titulo || '');
            setDescricao(examData.descricao || '');
            if (examData.dataInicio) setDataInicio(examData.dataInicio.substring(0, 16));
            if (examData.dataFim) setDataFim(examData.dataFim.substring(0, 16));
        }
    }, [examData, isEdit]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const data = {
            titulo,
            descricao: descricao || null,
            dataInicio: dataInicio ? new Date(dataInicio).toISOString() : null,
            dataFim: dataFim ? new Date(dataFim).toISOString() : null,
        };
        
        try {
            if (isEdit) {
                await updateMutation.mutateAsync({ id, data });
            } else {
                await createMutation.mutateAsync(data);
            }
            navigate('/');
        } catch (err) {
            // Error handling is managed by the mutation's error state below
            console.error(err);
        }
    };

    if (isEdit && isLoadingExam) return (
        <div className="flex justify-center py-20 text-slate-400">Carregando...</div>
    );
    if (isEdit && isExamError) return (
        <div className="text-center py-20 text-red-400">Erro ao carregar prova.</div>
    );

    const isPending = createMutation.isPending || updateMutation.isPending;
    const currentError = createMutation.error?.response?.data?.message || updateMutation.error?.response?.data?.message;

    return (
        <div className="max-w-2xl mx-auto p-6">
            <h1 className="text-2xl font-bold text-white mb-6">{isEdit ? 'Editar Prova' : 'Nova Prova'}</h1>
            {currentError && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{currentError}</div>}
            
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
                    <button type="submit" disabled={isPending}
                        className="px-6 py-2.5 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition disabled:opacity-50">
                        {isPending ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button type="button" onClick={() => navigate('/')}
                        className="px-6 py-2.5 rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition">Cancelar</button>
                </div>
            </form>
        </div>
    );
}
