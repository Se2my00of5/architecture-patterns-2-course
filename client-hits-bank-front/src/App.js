import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login/Login';
import OAuth2Callback from './components/Login/OAuth2Callback';
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
  const { user, isLoading } = useAuth();
  
  if (isLoading) {
    return (
      <div className="app-loading">
        <div className="loading-spinner-large"></div>
      </div>
    );
  }
  
  if (!user) {
    return (
      <Routes>
        <Route path="/login/oauth2/code/frontend-app" element={<OAuth2Callback />} />
        <Route path="*" element={<Login />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/login/oauth2/code/frontend-app" element={<OAuth2Callback />} />
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