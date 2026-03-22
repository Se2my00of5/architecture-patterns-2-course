import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { accountsApi } from '../../api/accounts';
import AccountOperationsModal from './AccountOperationsModal';
import CreateAccountModal from './CreateAccountModal';
import TransferModal from './TransferModal';
import './Accounts.css';

const Accounts = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [modalType, setModalType] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

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

  const handleCreateAccount = async (currency) => {
    const result = await accountsApi.createAccount(user.id, currency);
    
    if (result.success) {
      toast.success(`Счет в ${currency} успешно создан!`);
      setShowCreateModal(false);
      loadAccounts();
    } else {
      toast.error(result.error || 'Ошибка при создании счета');
    }
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

  const formatCurrency = (value, currency = 'RUB') => {
    if (value === undefined || value === null) return '0 ₽';
    const symbols = {
      RUB: '₽',
      USD: '$',
      CNY: '¥'
    };
    return `${Number(value).toLocaleString('ru-RU')} ${symbols[currency] || '₽'}`;
  };

  const handleOperation = async (accountId, amount, description) => {
    let result;
    
    if (modalType === 'deposit') {
      result = await accountsApi.deposit(accountId, amount, description);
    } else if (modalType === 'withdraw') {
      result = await accountsApi.withdraw(accountId, amount, description);
    }

    if (result.success) {
      toast.success('Операция поставлена в очередь');
      loadAccounts();
      setSelectedAccount(null);
      setModalType(null);
    } else {
      toast.error(result.error || 'Ошибка при выполнении операции');
    }
  };

  const handleTransfer = async (sourceAccountId, targetAccountId, amount, description) => {
    const result = await accountsApi.transfer(sourceAccountId, targetAccountId, amount, description);
    
    if (result.success) {
      toast.success('Перевод поставлен в очередь');
      loadAccounts();
      setSelectedAccount(null);
      setModalType(null);
    } else {
      toast.error(result.error || 'Ошибка при выполнении перевода');
    }
  };

  const handleCloseAccount = async (accountId) => {
    const result = await accountsApi.closeAccount(accountId);
    
    if (result.success) {
      toast.success('Счет успешно закрыт');
      loadAccounts();
    } else {
      toast.error(result.error || 'Ошибка при закрытии счета');
    }
  };

  const handleShowHistory = (accountId) => {
    navigate(`/accounts/${accountId}/history`);
  };

  const activeAccounts = accounts.filter(acc => acc.status === 'ACTIVE');
  const closedAccounts = accounts.filter(acc => acc.status !== 'ACTIVE');

  if (loading) {
    return (
      <div className="accounts-loading">
        <div className="loading-spinner-large"></div>
      </div>
    );
  }

  return (
    <div className="accounts">
      <div className="accounts-header">
        <h2>Ваши счета</h2>
        <div className="header-buttons">
          <button className="create-account-button" onClick={() => setShowCreateModal(true)}>
            + Открыть счет
          </button>
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
        </div>
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
                    <span className="label">balance:</span> {formatCurrency(account.balance, account.currency)}
                  </div>
                  <div className="account-currency">
                    <span className="label">валюта:</span> {account.currency}
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
                      className="action-button transfer"
                      onClick={() => {
                        setSelectedAccount(account);
                        setModalType('transfer');
                      }}
                    >
                      Перевод
                    </button>
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
                  <div className="account-currency">
                    <span className="label">валюта:</span> {account.currency}
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

      {selectedAccount && modalType === 'transfer' && (
        <TransferModal
          account={selectedAccount}
          accounts={accounts}
          onClose={() => {
            setSelectedAccount(null);
            setModalType(null);
          }}
          onSubmit={handleTransfer}
        />
      )}

      {selectedAccount && (modalType === 'deposit' || modalType === 'withdraw') && (
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

      {showCreateModal && (
        <CreateAccountModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateAccount}
        />
      )}
    </div>
  );
};

export default Accounts;