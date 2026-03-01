import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { authApi } from '../../api/auth';
import { storage } from '../../utils/storage';
import './Login.css';

const Login = ({ onLoginSuccess }) => {
  const [login, setLogin] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!login.trim()) {
      toast.error('Введите логин');
      return;
    }

    setIsLoading(true);
    
    try {
      const result = await authApi.login(login);
      
      if (result.success) {
        const userData = result.data;
        
        if (userData.isBlocked) {
          toast.error('Ваш аккаунт заблокирован');
          return;
        }
        
        if (userData.role !== 'EMPLOYEE') {
          toast.error('Доступ разрешен только сотрудникам банка');
          return;
        }
        
        storage.setAuthData({
          id: userData.id,
          login: userData.login,
          fullName: userData.fullName
        });
                
        if (onLoginSuccess) {
          onLoginSuccess(userData);
        }
        
      } else {
        if (result.status === 404) {
          toast.error('Пользователь не найден');
        } else {
          toast.error(result.error || 'Ошибка при входе');
        }
      }
    } catch (error) {
      toast.error('Произошла непредвиденная ошибка');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>
          <span>HITS</span> БАНК
        </h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="login">Логин</label>
            <input
              type="text"
              id="login"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
              placeholder="Введите логин"
              disabled={isLoading}
            />
          </div>
          <button 
            type="submit" 
            className="login-button"
            disabled={isLoading}
          >
            {isLoading ? 'Вход...' : 'Войти'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;