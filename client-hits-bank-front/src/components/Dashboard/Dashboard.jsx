import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../contexts/AuthContext';
import { accountsApi } from '../../api/accounts';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isCreatingAccount, setIsCreatingAccount] = useState(false);

  const handleLogout = () => {
    logout();
  };

  const handleOpenAccount = async () => {
    setIsCreatingAccount(true);
    
    try {
      const result = await accountsApi.createAccount(user.id);
      
      if (result.success && result.status === 201) {
        toast.success('Счет успешно создан!');
      } else {
        toast.error('Ошибка при создании счета');
      }
    } catch (error) {
      toast.error('Ошибка при создании счета');
    } finally {
      setIsCreatingAccount(false);
    }
  };

  const handleYourAccounts = () => {
    navigate('/accounts');
  };

  const handleTakeCredit = () => {
  };

  const handleYourCredits = () => {
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>
            <span>HITS</span> БАНК
          </h1>
          <div className="user-info">
            <span>
              <span>{user?.fullName}</span>
            </span>
            <button onClick={handleLogout} className="logout-button">
              Выйти
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <h2>Личный кабинет клиента</h2>
        
        <div className="menu-grid">
          <button 
            className="menu-button open-account"
            onClick={handleOpenAccount}
            disabled={isCreatingAccount}
          >
            <span>
              Открыть счет
              {isCreatingAccount && <span className="loading-spinner"></span>}
            </span>
          </button>

          <button 
            className="menu-button accounts"
            onClick={handleYourAccounts}
          >
            <span>Ваши счета</span>
          </button>

          <button 
            className="menu-button credit"
            onClick={handleTakeCredit}
          >
            <span>Взять кредит</span>
          </button>

          <button 
            className="menu-button my-credits"
            onClick={handleYourCredits}
          >
            <span>Ваши кредиты</span>
          </button>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;