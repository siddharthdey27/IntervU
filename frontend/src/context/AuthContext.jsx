import { createContext, useContext, useState } from 'react';
import * as authApi from '../api/auth.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const persistSession = (data) => {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const userInfo = { userId: data.userId, fullName: data.fullName, email: data.email };
    localStorage.setItem('user', JSON.stringify(userInfo));
    setUser(userInfo);
  };

  const login = async (email, password) => {
    const data = await authApi.login(email, password);
    persistSession(data);
  };

  const register = async (fullName, email, password) => {
    const data = await authApi.register(fullName, email, password);
    persistSession(data);
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
