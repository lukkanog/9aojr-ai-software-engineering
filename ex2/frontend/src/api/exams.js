import api from './client';

export const getExams = () => api.get('/exams');
export const getExam = (id) => api.get(`/exams/${id}`);
export const createExam = (data) => api.post('/exams', data);
export const updateExam = (id, data) => api.put(`/exams/${id}`, data);
export const deleteExam = (id) => api.delete(`/exams/${id}`);
export const publishExam = (id) => api.post(`/exams/${id}/publish`);
export const closeExam = (id) => api.post(`/exams/${id}/close`);

export const getQuestions = (examId) => api.get(`/exams/${examId}/questions`);
export const addQuestion = (examId, data) => api.post(`/exams/${examId}/questions`, data);
export const updateQuestion = (id, data) => api.put(`/questions/${id}`, data);
export const deleteQuestion = (id) => api.delete(`/questions/${id}`);

export const getAnswerKey = (examId) => api.get(`/exams/${examId}/answer-key`);
export const createAnswerKey = (examId, data) => api.post(`/exams/${examId}/answer-key`, data);
export const updateAnswerKey = (examId, data) => api.put(`/exams/${examId}/answer-key`, data);
