export const AUTH_KEYS = ['accessToken', 'refreshToken', 'user'];

export function getStoredUser() {
  try {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  } catch { return null; }
}

export function persistSession(data) {
  const user = { userId: data.userId, fullName: data.fullName, email: data.email };
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('user', JSON.stringify(user));
  window.dispatchEvent(new CustomEvent('auth:session-updated', { detail: user }));
  return user;
}

export function clearSession() {
  AUTH_KEYS.forEach((key) => localStorage.removeItem(key));
  window.dispatchEvent(new Event('auth:session-cleared'));
}

function tokenExpired(token) {
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return !payload.exp || payload.exp * 1000 <= Date.now();
  } catch { return true; }
}

export function hasUsableSession() {
  const user = getStoredUser();
  const accessToken = localStorage.getItem('accessToken');
  const refreshToken = localStorage.getItem('refreshToken');
  return Boolean(user && ((!tokenExpired(accessToken)) || (!tokenExpired(refreshToken))));
}
