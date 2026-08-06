import client from './client.js';

export const getProgress = () =>
  client.get('/progress').then((r) => r.data);
