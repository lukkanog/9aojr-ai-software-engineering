import api from './client';

export const login = (email, senha) => api.post('/auth/login', { email, senha });
export const register = (nome, email, senha, role) => api.post('/auth/register', { nome, email, senha, role });
export const getMe = () => api.get('/auth/me');
export const logout = () => api.post('/auth/logout');
