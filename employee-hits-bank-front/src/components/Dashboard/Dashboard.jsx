import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
  };

  const handleClientList = () => {
    navigate('/clients');
  };

  const handleEmployeeList = () => {
    navigate('/employees');
  };

  const handleCreateTariff = () => {
    navigate('/tariffs/create');
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
              {user?.fullName}
            </span>
            <button onClick={handleLogout} className="logout-button">
              Выйти
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <h2>Личный кабинет сотрудника</h2>
        
        <div className="menu-grid employee-grid">
          <button 
            className="menu-button clients"
            onClick={handleClientList}
          >
            <span>Список клиентов</span>
          </button>

          <button 
            className="menu-button employees"
            onClick={handleEmployeeList}
          >
            <span>Список сотрудников</span>
          </button>

          <button 
            className="menu-button tariffs"
            onClick={handleCreateTariff}
          >
            <span>Создать тариф</span>
          </button>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;