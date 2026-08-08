import client from './client';

export const ingestKnowledgeDoc = (docData) =>
  client.post('/knowledge/ingest', docData);

export const listKnowledgeDocs = (companyName) =>
  client.get('/knowledge', { params: { companyName } });
