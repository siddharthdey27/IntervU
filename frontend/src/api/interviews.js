import client from './client.js';

export const startSession = (payload) =>
  client.post('/interviews/start', payload).then((r) => r.data);

export const sendMessage = (sessionId, userMessage) =>
  client.post('/interviews/message', { sessionId, userMessage }).then((r) => r.data);

export const getTranscript = (sessionId) =>
  client.get(`/interviews/${sessionId}/transcript`).then((r) => r.data);
