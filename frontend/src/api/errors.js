export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (!error) return fallback;
  const data = error.response?.data;
  if (typeof data === 'string' && data.trim()) return data;
  if (data?.error) return data.error;
  if (data?.message) return data.message;
  if (error.response?.status === 429) return 'Too many requests. Please wait a moment and try again.';
  if (error.response?.status === 403) return 'You do not have permission to perform this action.';
  if (error.response?.status === 404) return 'The requested resource was not found.';
  if (!error.response) return 'Unable to reach the server. Check your connection and try again.';
  return error.message || fallback;
}
