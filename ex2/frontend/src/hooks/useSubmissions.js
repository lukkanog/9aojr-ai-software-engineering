import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as subsApi from '../api/submissions';

export function useSubmissions(examId) {
    return useQuery({
        queryKey: ['submissions', examId],
        queryFn: async () => {
            const res = await subsApi.getSubmissions(examId);
            return res.data;
        },
        enabled: !!examId
    });
}

export function useCorrectionResult(submissionId) {
    return useQuery({
        queryKey: ['correctionResult', submissionId],
        queryFn: async () => {
            const res = await subsApi.getCorrectionResult(submissionId);
            return res.data;
        },
        enabled: !!submissionId
    });
}

export function useReport(examId) {
    return useQuery({
        queryKey: ['report', examId],
        queryFn: async () => {
            const res = await subsApi.getReport(examId);
            return res.data;
        },
        enabled: !!examId
    });
}

export function useStatistics(examId) {
    return useQuery({
        queryKey: ['statistics', examId],
        queryFn: async () => {
            const res = await subsApi.getStatistics(examId);
            return res.data;
        },
        enabled: !!examId
    });
}

export function useIssues(questionId) {
    return useQuery({
        queryKey: ['issues', questionId],
        queryFn: async () => {
            const res = await subsApi.getIssues(questionId);
            return res.data;
        },
        enabled: !!questionId
    });
}

export function useCreateSubmission() {
    return useMutation({
        mutationFn: async ({ examId, data }) => {
            const res = await subsApi.createSubmission(examId, data);
            return res.data;
        }
    });
}

export function useCorrectSubmission() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id) => {
            const res = await subsApi.correctSubmission(id);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['submissions'] });
            queryClient.invalidateQueries({ queryKey: ['correctionResult', variables] });
        }
    });
}

export function useCreateIssue() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({ questionId, data }) => {
            const res = await subsApi.createIssue(questionId, data);
            return res.data;
        },
        onSuccess: (data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['issues', variables.questionId] });
        }
    });
}
