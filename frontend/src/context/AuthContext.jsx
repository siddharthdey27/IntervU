import { createContext, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/auth.js';
import { clearSession, getStoredUser, hasUsableSession, persistSession } from '../auth/session.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  useEffect(() => {
    const onUpdated = (event) => setUser(event.detail || getStoredUser());
    const onCleared = () => setUser(null);
    window.addEventListener('auth:session-updated', onUpdated);
    window.addEventListener('auth:session-cleared', onCleared);
    if (!hasUsableSession()) clearSession();
    setUser(hasUsableSession() ? getStoredUser() : null);
    setIsAuthLoading(false);
    return () => {
      window.removeEventListener('auth:session-updated', onUpdated);
      window.removeEventListener('auth:session-cleared', onCleared);
    };
  }, []);

  const login = async (email, password) => {
    const data = await authApi.login(email, password);
    persistSession(data);
  };

  const register = async (fullName, email, password) => {
    const data = await authApi.register(fullName, email, password);
    persistSession(data);
  };

  const logout = () => {
    clearSession();
  };

  return (
    <AuthContext.Provider value={{ user, isAuthLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
