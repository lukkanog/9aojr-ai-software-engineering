import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Navbar() {
    const { user, logoutUser, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logoutUser();
        navigate('/login');
    };

    if (!isAuthenticated) return null;

    return (
        <nav className="bg-slate-900 text-white shadow-lg">
            <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
                <Link to="/" className="text-xl font-bold tracking-tight bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
                    📝 ExamCorrection
                </Link>
                <div className="flex items-center gap-4">
                    <span className="text-sm text-slate-300">
                        {user?.nome} <span className="text-xs px-2 py-0.5 rounded-full bg-blue-600/30 text-blue-300">{user?.role}</span>
                    </span>
                    <button onClick={handleLogout} className="text-sm px-3 py-1.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition">
                        Sair
                    </button>
                </div>
            </div>
        </nav>
    );
}
