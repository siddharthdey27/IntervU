import client from './client.js';

export const login = (email, password) =>
  client.post('/auth/login', { email, password }).then((r) => r.data);

export const register = (fullName, email, password) =>
  client.post('/auth/register', { fullName, email, password }).then((r) => r.data);
