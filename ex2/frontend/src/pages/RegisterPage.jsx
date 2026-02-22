import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as authApi from '../api/auth';

export default function RegisterPage() {
    const [nome, setNome] = useState('');
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [role, setRole] = useState('ALUNO');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { loginUser } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const res = await authApi.register(nome, email, senha, role);
            loginUser(res.data);
            navigate('/');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao cadastrar.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
            <div className="w-full max-w-md p-8 bg-slate-800/50 backdrop-blur-sm rounded-2xl shadow-2xl border border-slate-700/50">
                <h1 className="text-3xl font-bold text-center mb-2 bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">Cadastro</h1>
                <p className="text-center text-slate-400 mb-8">Crie sua conta</p>
                {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-sm">{error}</div>}
                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label htmlFor="nome" className="block text-sm font-medium text-slate-300 mb-1">Nome</label>
                        <input id="nome" value={nome} onChange={e => setNome(e.target.value)} required
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none transition" />
                    </div>
                    <div>
                        <label htmlFor="reg-email" className="block text-sm font-medium text-slate-300 mb-1">Email</label>
                        <input id="reg-email" type="email" value={email} onChange={e => setEmail(e.target.value)} required
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none transition" />
                    </div>
                    <div>
                        <label htmlFor="reg-senha" className="block text-sm font-medium text-slate-300 mb-1">Senha</label>
                        <input id="reg-senha" type="password" value={senha} onChange={e => setSenha(e.target.value)} required
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none transition" />
                    </div>
                    <div>
                        <label htmlFor="role" className="block text-sm font-medium text-slate-300 mb-1">Tipo de conta</label>
                        <select id="role" value={role} onChange={e => setRole(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-lg bg-slate-700/50 border border-slate-600 text-white focus:ring-2 focus:ring-blue-500 outline-none transition">
                            <option value="ALUNO">Aluno</option>
                            <option value="PROFESSOR">Professor</option>
                        </select>
                    </div>
                    <button type="submit" disabled={loading}
                        className="w-full py-2.5 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-500 hover:to-purple-500 transition disabled:opacity-50">
                        {loading ? 'Cadastrando...' : 'Cadastrar'}
                    </button>
                </form>
                <p className="mt-6 text-center text-sm text-slate-400">
                    Já tem conta? <Link to="/login" className="text-blue-400 hover:underline">Entrar</Link>
                </p>
            </div>
        </div>
    );
}
