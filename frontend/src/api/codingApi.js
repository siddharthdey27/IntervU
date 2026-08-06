import client from "./client.js";

export const listQuestions = () =>
  client.get("/coding-questions").then((res) => res.data);

export const getQuestion = (id) =>
  client.get(`/coding-questions/${id}`).then((res) => res.data);

export const runCode = (questionId, { language, sourceCode, stdin }) =>
  client
    .post(`/coding-questions/${questionId}/run`, { language, sourceCode, stdin })
    .then((res) => res.data);

export const submitCode = (questionId, { language, sourceCode }) =>
  client
    .post(`/coding-questions/${questionId}/submit`, { language, sourceCode })
    .then((res) => res.data);
