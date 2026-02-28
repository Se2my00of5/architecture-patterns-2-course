import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../../api/users';
import './ClientAccounts.css';

const ClientAccounts = () => {
  const { clientId } = useParams();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [clientInfo, setClientInfo] = useState(null);

  useEffect(() => {
    loadAccounts();
  }, [clientId]);

  const loadAccounts = async () => {
    setLoading(true);
    const result = await usersApi.getUserAccounts(clientId);
    if (result.success) {
      setAccounts(result.data);
      if (result.data.length > 0) {
        setClientInfo({ id: result.data[0].userId });
      }
    } else {
      toast.error('Ошибка при загрузке счетов');
    }
    setLoading(false);
  };

  const handleShowHistory = (accountId) => {
    navigate(`/clients/${clientId}/accounts/${accountId}/history`);
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

  const activeAccounts = accounts.filter(acc => acc.status === 'ACTIVE');
  const closedAccounts = accounts.filter(acc => acc.status !== 'ACTIVE');

  return (
    <div className="client-accounts">
      <div className="accounts-header">
        <div className="accounts-header-top">
          <button className="back-button" onClick={() => navigate('/clients')}>
            ← Назад к клиентам
          </button>
        </div>
        <h2>Счета клиента</h2>
        <div className="client-id-display">
          ID клиента: {clientId}
        </div>
      </div>

      {activeAccounts.length > 0 && (
        <div className="accounts-section">
          <h3>Активные счета:</h3>
          <div className="accounts-list">
            {activeAccounts.map(account => (
              <div key={account.id} className="account-card">
                <div className="account-info">
                  <div className="account-id">
                    <span className="label">id:</span> {account.id}
                  </div>
                  <div className="account-balance">
                    <span className="label">баланс:</span> {account.balance} ₽
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
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {closedAccounts.length > 0 && (
        <div className="accounts-section">
          <h3>Закрытые счета:</h3>
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
          <p>У клиента нет счетов</p>
        </div>
      )}
    </div>
  );
};

export default ClientAccounts;