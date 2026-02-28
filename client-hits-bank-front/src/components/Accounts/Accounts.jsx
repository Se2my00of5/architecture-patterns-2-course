import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { accountsApi } from '../../api/accounts';
import AccountOperationsModal from './AccountOperationsModal';
import './Accounts.css';

const Accounts = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [setLoading] = useState(true);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [modalType, setModalType] = useState(null); 

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    setLoading(true);
    const result = await accountsApi.getUserAccounts(user.id);
    if (result.success) {
      setAccounts(result.data);
    } else {
      toast.error('Ошибка при загрузке счетов');
    }
    setLoading(false);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleOperation = async (accountId, amount, description) => {
    let result;
    
    if (modalType === 'deposit') {
      result = await accountsApi.deposit(accountId, amount, description);
    } else if (modalType === 'withdraw') {
      result = await accountsApi.withdraw(accountId, amount, description);
    }

    if (result.success) {
      toast.success('Операция успешна');
      setAccounts(prevAccounts => 
        prevAccounts.map(acc => 
          acc.id === accountId ? result.data : acc
        )
      );
      setSelectedAccount(null);
      setModalType(null);
    } else {
      toast.error(result.error || 'Ошибка при выполнении операции');
    }
  };

  const handleCloseAccount = async (accountId) => {
    const result = await accountsApi.closeAccount(accountId);
    
    if (result.success) {
      toast.success('Счет успешно закрыт');
      setAccounts(prevAccounts => 
        prevAccounts.map(acc => 
          acc.id === accountId ? { ...acc, status: 'CLOSED', closedAt: new Date().toISOString() } : acc
        )
      );
      setSelectedAccount(null);
    } else {
      toast.error(result.error || 'Ошибка при закрытии счета');
    }
  };

  const handleShowHistory = (accountId) => {
    navigate(`/accounts/${accountId}/history`);
  };

  const activeAccounts = accounts.filter(acc => acc.status === 'ACTIVE');
  const closedAccounts = accounts.filter(acc => acc.status !== 'ACTIVE');

  return (
    <div className="accounts">
      <div className="accounts-header">
        <h2>Ваши счета</h2>
        <button className="back-button" onClick={() => navigate('/dashboard')}>
          ← Назад
        </button>
      </div>

      {activeAccounts.length > 0 && (
        <div className="accounts-section">
          <h3>Активные:</h3>
          <div className="accounts-list">
            {activeAccounts.map(account => (
              <div key={account.id} className="account-card">
                <div className="account-info">
                  <div className="account-id">
                    <span className="label">id:</span> {account.id}
                  </div>
                  <div className="account-balance">
                    <span className="label">balance:</span> {account.balance} ₽
                  </div>
                  <div className="account-date">
                    <span className="label">открыт:</span> {formatDate(account.createdAt)}
                  </div>
                </div>
                
                <div className="account-actions">
                  <button 
                    className="action-button history"
                    onClick={() => handleShowHistory(account.id)}
                  >
                    История
                  </button>
                  <div className="amount-actions">
                    <button 
                      className="action-button withdraw"
                      onClick={() => {
                        setSelectedAccount(account);
                        setModalType('withdraw');
                      }}
                    >
                      Снять
                    </button>
                    <button 
                      className="action-button deposit"
                      onClick={() => {
                        setSelectedAccount(account);
                        setModalType('deposit');
                      }}
                    >
                      Внести
                    </button>
                  </div>
                  <button 
                    className="action-button close"
                    onClick={() => handleCloseAccount(account.id)}
                  >
                    Закрыть
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {closedAccounts.length > 0 && (
        <div className="accounts-section">
          <h3>Закрытые:</h3>
          <div className="accounts-list">
            {closedAccounts.map(account => (
              <div key={account.id} className="account-card closed">
                <div className="account-info">
                  <div className="account-id">
                    <span className="label">id:</span> {account.id}
                  </div>
                  <div className="account-date">
                    <span className="label">открыт:</span> {formatDate(account.createdAt)}
                  </div>
                  <div className="account-date">
                    <span className="label">закрыт:</span> {formatDate(account.closedAt)}
                  </div>
                </div>
                <div className="account-actions">
                  <button 
                    className="action-button history"
                    onClick={() => handleShowHistory(account.id)}
                  >
                    История
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {accounts.length === 0 && (
        <div className="no-accounts">
          <p>У вас пока нет счетов</p>
        </div>
      )}

      {selectedAccount && modalType && (
        <AccountOperationsModal
          account={selectedAccount}
          type={modalType}
          onClose={() => {
            setSelectedAccount(null);
            setModalType(null);
          }}
          onSubmit={handleOperation}
        />
      )}
    </div>
  );
};

export default Accounts;