import { createContext, useContext, useState, useEffect } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    });

    const [token, setToken] = useState(() => localStorage.getItem('token'));
    const [isReauthModalOpen, setIsReauthModalOpen] = useState(false);
    
    // Using simple states for the modal form
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        const handleUnauthorized = () => {
            setIsReauthModalOpen(true);
        };
        window.addEventListener('auth:unauthorized', handleUnauthorized);
        return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
    }, []);

    const loginUser = (authResponse) => {
        localStorage.setItem('token', authResponse.token);
        localStorage.setItem('user', JSON.stringify(authResponse));
        setToken(authResponse.token);
        setUser(authResponse);
        setIsReauthModalOpen(false);
        setError('');
    };

    const logoutUser = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
        setIsReauthModalOpen(false);
    };

    const handleReauth = async (e) => {
        e.preventDefault();
        try {
            const res = await authApi.login(email, password);
            loginUser(res.data);
            setEmail('');
            setPassword('');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao autenticar. Verifique suas credenciais.');
        }
    };

    return (
        <AuthContext.Provider value={{ user, token, loginUser, logoutUser, isAuthenticated: !!token }}>
            {children}
            
            {isReauthModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
                    <div className="bg-slate-800 p-8 rounded-2xl w-full max-w-sm border border-slate-700 shadow-2xl">
                        <h2 className="text-xl font-bold text-white mb-2">Sessão Expirada</h2>
                        <p className="text-slate-400 text-sm mb-6">Por favor, faça login novamente para continuar de onde parou sem perder seu estado atual.</p>
                        
                        {error && <div className="p-3 mb-4 text-sm text-red-400 bg-red-400/10 rounded-lg">{error}</div>}
                        
                        <form onSubmit={handleReauth} className="space-y-4">
                            <div>
                                <label className="block text-sm text-slate-400 mb-1">Email</label>
                                <input type="email" value={email} onChange={e => setEmail(e.target.value)} required 
                                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-4 py-2 text-white outline-none focus:border-blue-500" />
                            </div>
                            <div>
                                <label className="block text-sm text-slate-400 mb-1">Senha</label>
                                <input type="password" value={password} onChange={e => setPassword(e.target.value)} required 
                                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-4 py-2 text-white outline-none focus:border-blue-500" />
                            </div>
                            <div className="flex gap-3 mt-6">
                                <button type="button" onClick={logoutUser}
                                    className="flex-1 px-4 py-2 text-sm font-medium text-slate-300 bg-slate-700/50 rounded-lg hover:bg-slate-700 transition">
                                    Sair
                                </button>
                                <button type="submit"
                                    className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-500 transition">
                                    Voltar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
}
