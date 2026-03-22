import React, { createContext, useState, useContext, useEffect } from 'react';
import { oauthService } from '../api/oauthService'; 

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const tokens = localStorage.getItem('oauth_tokens');
    
    if (savedUser && tokens) {
      const userData = JSON.parse(savedUser);
      setUser(userData);
    }
    setIsLoading(false);
  }, []);

  const login = (userData) => {
    setUser(userData);
  };

    const logout = async () => {
    try {
      const tokens = localStorage.getItem('oauth_tokens');
      let refreshToken = null;
      
      if (tokens) {
        const parsed = JSON.parse(tokens);
        refreshToken = parsed.refreshToken;
      }
      
      await fetch('http://localhost:1115/api/auth/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${oauthService.getAccessToken()}`
        },
        body: JSON.stringify({
          refreshToken: refreshToken
        }),
        credentials: 'include'
      });
    } catch (error) {
      console.error('Logout error:', error);
    }
    
    localStorage.removeItem('oauth_tokens');
    localStorage.removeItem('user');
    sessionStorage.clear();
    
    window.location.href = '/';
  };

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};