import client from './client.js';

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return client
    .post('/resumes/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((r) => r.data);
};

export const listResumes = () => client.get('/resumes').then((r) => r.data);
