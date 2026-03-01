import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login/Login';
import Dashboard from './components/Dashboard/Dashboard';
import Clients from './components/Clients/Clients';
import ClientAccounts from './components/Clients/ClientAccounts';
import AccountHistory from './components/Clients/AccountHistory';
import Employees from './components/Employees/Employees';
import Tariffs from './components/Tariffs/Tariffs';
import ClientCredits from './components/Clients/ClientCredits';

import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
        <ToastContainer 
          position="bottom-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="colored"
        />
      </AuthProvider>
    </Router>
  );
}

const AppContent = () => {
  const { user, login } = useAuth();
  
  const handleLoginSuccess = (userData) => {
    login(userData);
  };
  
  if (!user) {
    return (
      <Routes>
        <Route path="*" element={<Login onLoginSuccess={handleLoginSuccess} />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/clients" element={<Clients />} />
      <Route path="/clients/:clientId/accounts" element={<ClientAccounts />} />
      <Route path="/clients/:clientId/accounts/:accountId/history" element={<AccountHistory />} />
      <Route path="/clients/:clientId/credits" element={<ClientCredits />} />
      <Route path="/employees" element={<Employees />} />
      <Route path="/tariffs" element={<Tariffs />} />
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
};

export default App;