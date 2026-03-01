import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login/Login';
import Dashboard from './components/Dashboard/Dashboard';
import Accounts from './components/Accounts/Accounts';
import AccountHistory from './components/Accounts/AccountHistory';
import Credits from './components/Credits/Credits';
import ClientCredits from './components/Credits/ClientCredits';

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
      <Route path="/accounts" element={<Accounts />} />
      <Route path="/accounts/:accountId/history" element={<AccountHistory />} />
      <Route path="/credit" element={<Credits />} />
      <Route path="/client-credits" element={<ClientCredits />} />
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
};

export default App;