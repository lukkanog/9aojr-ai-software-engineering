import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as examsApi from '../api/exams';

export function useExams() {
    return useQuery({
        queryKey: ['exams'],
        queryFn: async () => {
            const res = await examsApi.getExams();
            return res.data;
        }
    });
}

export function useExam(id) {
    return useQuery({
        queryKey: ['exam', id],
        queryFn: async () => {
            const res = await examsApi.getExam(id);
            return res.data;
        },
        enabled: !!id
    });
}

export function useDeleteExam() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id) => {
            await examsApi.deleteExam(id);
            return id;
        },
        onSuccess: (deletedId) => {
            queryClient.setQueryData(['exams'], (oldExams) => {
                if (!oldExams) return oldExams;
                return oldExams.filter(e => e.id !== deletedId);
            });
            queryClient.invalidateQueries({ queryKey: ['exam', deletedId] });
        }
    });
}

export function useCreateExam() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (data) => {
            const res = await examsApi.createExam(data);
            return res.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['exams'] });
        }
    });
}

export function useUpdateExam() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({ id, data }) => {
            const res = await examsApi.updateExam(id, data);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['exams'] });
            queryClient.invalidateQueries({ queryKey: ['exam', variables.id] });
        }
    });
}

export function usePublishExam() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id) => {
            const res = await examsApi.publishExam(id);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['exams'] });
            queryClient.invalidateQueries({ queryKey: ['exam', variables] });
        }
    });
}

export function useCloseExam() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id) => {
            const res = await examsApi.closeExam(id);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['exams'] });
            queryClient.invalidateQueries({ queryKey: ['exam', variables] });
        }
    });
}

// ---- QUESTIONS ----

export function useQuestions(examId) {
    return useQuery({
        queryKey: ['questions', examId],
        queryFn: async () => {
            const res = await examsApi.getQuestions(examId);
            return res.data;
        },
        enabled: !!examId
    });
}

export function useAddQuestion() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({ examId, data }) => {
            const res = await examsApi.addQuestion(examId, data);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['questions', variables.examId] });
            queryClient.invalidateQueries({ queryKey: ['exam', variables.examId] });
        }
    });
}

export function useDeleteQuestion() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({ id, examId }) => {
            await examsApi.deleteQuestion(id);
            return { id, examId };
        },
        onSuccess: ({ id, examId }) => {
            queryClient.invalidateQueries({ queryKey: ['questions', examId] });
            queryClient.invalidateQueries({ queryKey: ['exam', examId] });
        }
    });
}

// ---- ANSWER KEY ----

export function useAnswerKey(examId) {
    return useQuery({
        queryKey: ['answerKey', examId],
        queryFn: async () => {
            try {
                const res = await examsApi.getAnswerKey(examId);
                return res.data;
            } catch (err) {
                if (err.response?.status === 404) return null;
                throw err;
            }
        },
        enabled: !!examId,
        retry: false
    });
}

export function useSaveAnswerKey() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({ examId, data, isUpdate }) => {
            let res;
            if (isUpdate) {
                res = await examsApi.updateAnswerKey(examId, data);
            } else {
                res = await examsApi.createAnswerKey(examId, data);
            }
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['answerKey', variables.examId] });
            queryClient.invalidateQueries({ queryKey: ['exam', variables.examId] });
        }
    });
}
