import api from './client';

export const getSubmissions = (examId) => api.get(`/exams/${examId}/submissions`);
export const getSubmission = (id) => api.get(`/submissions/${id}`);
export const createSubmission = (examId, data) => api.post(`/exams/${examId}/submissions`, data);

export const correctSubmission = (id) => api.post(`/submissions/${id}/correct`);
export const getCorrectionResult = (id) => api.get(`/submissions/${id}/correction-result`);

export const getReport = (examId) => api.get(`/exams/${examId}/report`);
export const getStatistics = (examId) => api.get(`/exams/${examId}/statistics`);

export const getIssues = (questionId) => api.get(`/questions/${questionId}/issues`);
export const createIssue = (questionId, data) => api.post(`/questions/${questionId}/issues`, data);
