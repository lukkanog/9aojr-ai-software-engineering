import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ExamListPage from './pages/ExamListPage';
import ExamDetailPage from './pages/ExamDetailPage';
import ExamFormPage from './pages/ExamFormPage';
import QuestionsPage from './pages/QuestionsPage';
import AnswerKeyPage from './pages/AnswerKeyPage';
import CorrectionResultPage from './pages/CorrectionResultPage';
import ReportPage from './pages/ReportPage';
import IssuesPage from './pages/IssuesPage';

function AppRoutes() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <Navbar />
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" />} />
        <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/" />} />
        <Route path="/" element={<ProtectedRoute><ExamListPage /></ProtectedRoute>} />
        <Route path="/exams/new" element={<ProtectedRoute roles={['PROFESSOR']}><ExamFormPage /></ProtectedRoute>} />
        <Route path="/exams/:id/edit" element={<ProtectedRoute roles={['PROFESSOR']}><ExamFormPage /></ProtectedRoute>} />
        <Route path="/exams/:id" element={<ProtectedRoute><ExamDetailPage /></ProtectedRoute>} />
        <Route path="/exams/:examId/questions" element={<ProtectedRoute roles={['PROFESSOR']}><QuestionsPage /></ProtectedRoute>} />
        <Route path="/exams/:examId/answer-key" element={<ProtectedRoute roles={['PROFESSOR']}><AnswerKeyPage /></ProtectedRoute>} />
        <Route path="/exams/:examId/report" element={<ProtectedRoute roles={['PROFESSOR']}><ReportPage /></ProtectedRoute>} />
        <Route path="/exams/:examId/statistics" element={<ProtectedRoute roles={['PROFESSOR']}><ReportPage /></ProtectedRoute>} />
        <Route path="/exams/:examId/questions/:questionId/issues" element={<ProtectedRoute roles={['PROFESSOR']}><IssuesPage /></ProtectedRoute>} />
        <Route path="/submissions/:submissionId/result" element={<ProtectedRoute><CorrectionResultPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
