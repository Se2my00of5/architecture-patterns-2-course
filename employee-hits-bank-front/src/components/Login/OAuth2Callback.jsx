import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { oauthService } from '../../api/oauthService';
import { useAuth } from '../../contexts/AuthContext';
import './OAuth2Callback.css';

const OAuth2Callback = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState(null);
  const processed = useRef(false);

  useEffect(() => {
    const handleCallback = async () => {
      if (processed.current) {
        console.log('Callback already processed, skipping');
        return;
      }
      processed.current = true;

      const params = new URLSearchParams(location.search);
      const code = params.get('code');
      const state = params.get('state');
      const errorParam = params.get('error');

      if (errorParam) {
        setError(`Ошибка авторизации: ${errorParam}`);
        toast.error(`Ошибка авторизации: ${errorParam}`);
        setTimeout(() => navigate('/'), 2000);
        return;
      }

      if (!code || !state) {
        setError('Неверный callback URL');
        toast.error('Неверный callback URL');
        setTimeout(() => navigate('/'), 2000);
        return;
      }

      try {
        const result = await oauthService.handleCallback(code, state);
        console.log('Callback result:', result);
        
        if (result.success) {
          login(result.user);
          toast.success(`Добро пожаловать, ${result.user.fullName}!`);
          navigate('/dashboard');
        } else {
          setError(result.error || 'Ошибка при входе');
          toast.error(result.error || 'Ошибка при входе');
          setTimeout(() => navigate('/'), 2000);
        }
      } catch (err) {
        setError(err.message);
        toast.error(err.message);
        setTimeout(() => navigate('/'), 2000);
      }
    };

    handleCallback();
  }, [location, navigate, login]);

  if (error) {
    return (
      <div className="oauth-callback error">
        <h2>Ошибка входа</h2>
        <p>{error}</p>
        <p>Перенаправление на страницу входа...</p>
      </div>
    );
  }

  return (
    <div className="oauth-callback">
      <div className="loading-spinner"></div>
      <h2>Выполняется вход</h2>
      <p>Пожалуйста, подождите...</p>
    </div>
  );
};

export default OAuth2Callback;