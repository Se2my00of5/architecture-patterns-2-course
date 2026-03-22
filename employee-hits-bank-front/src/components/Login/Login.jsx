import React from 'react';
import { oauthService } from '../../api/oauthService';
import './Login.css';

const Login = () => {
  const handleLogin = () => {
    oauthService.initiateLogin();
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>
          <span>HITS</span> БАНК
        </h1>
        <button 
          type="button" 
          className="login-button"
          onClick={handleLogin}
        >
          Войти
        </button>
      </div>
    </div>
  );
};

export default Login;