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

const PrivateRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/" />;
};

const AppContent = () => {
  const { isAuthenticated, login } = useAuth();

  const handleLoginSuccess = (userData) => {
    login(userData);
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={
          isAuthenticated ? <Navigate to="/dashboard" /> : <Login onLoginSuccess={handleLoginSuccess} />
        } />
        <Route path="/dashboard" element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        } />
        <Route path="/clients" element={
          <PrivateRoute>
            <Clients />
          </PrivateRoute>
        } />
        <Route path="/clients/:clientId/accounts" element={
          <PrivateRoute>
            <ClientAccounts />
          </PrivateRoute>
        } />
        <Route path="/clients/:clientId/accounts/:accountId/history" element={
          <PrivateRoute>
            <AccountHistory />
          </PrivateRoute>
        } />
        <Route path="/clients/:clientId/credits" element={
          <PrivateRoute>
            <ClientCredits />
          </PrivateRoute>
        } />
        <Route path="/employees" element={
          <PrivateRoute>
            <Employees />
          </PrivateRoute>
        } />
        <Route path="/tariffs" element={
          <PrivateRoute>
            <Tariffs />
          </PrivateRoute>
        } />
      </Routes>
    </Router>
  );
};

function App() {
  return (
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
        style={{ bottom: '1rem', right: '1rem' }}
      />
    </AuthProvider>
  );
}

export default App;