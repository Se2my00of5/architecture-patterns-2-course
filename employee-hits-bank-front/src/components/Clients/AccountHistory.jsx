import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../../api/users';
import { websocketService } from '../../api/websocketService';
import './AccountHistory.css';

const AccountHistory = () => {
  const { clientId, accountId } = useParams();
  const navigate = useNavigate();
  const [operations, setOperations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currency, setCurrency] = useState('RUB');

  useEffect(() => {
    loadAccountInfo();
    loadOperations();
    
    // Подключаем WebSocket и подписываемся на операции по этому счету
    websocketService.connect();
    websocketService.subscribeToAccount(accountId, (operation) => {
      console.log('New operation received:', operation);
      setOperations(prevOperations => [operation, ...prevOperations]);
      toast.info(`Новая операция: ${operation.type} на сумму ${operation.amount}`);
    });
    
    return () => {
      websocketService.unsubscribeFromAccount(accountId);
    };
  }, [accountId]);

  const loadAccountInfo = async () => {
    try {
      const result = await usersApi.getUserAccounts(clientId);
      if (result.success) {
        const account = result.data.find(acc => acc.id === accountId);
        if (account) {
          setCurrency(account.currency || 'RUB');
        }
      }
    } catch (error) {
      console.error('Error loading account info:', error);
    }
  };

  const loadOperations = async () => {
    setLoading(true);
    const result = await usersApi.getAccountOperations(accountId);
    if (result.success) {
      setOperations(result.data);
    } else {
      toast.error('Ошибка при загрузке истории операций');
    }
    setLoading(false);
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  const formatCurrency = (amount, currencyCode = currency) => {
    if (amount === undefined || amount === null) return '0 ₽';
    const symbols = {
      RUB: '₽',
      USD: '$',
      CNY: '¥'
    };
    return `${Number(amount).toLocaleString('ru-RU')} ${symbols[currencyCode] || '₽'}`;
  };

  const getOperationTypeText = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return 'Пополнение';
      case 'WITHDRAWAL':
        return 'Снятие';
      case 'TRANSFER_OUT':
        return 'Исходящий перевод';
      case 'TRANSFER_IN':
        return 'Входящий перевод';
      case 'LOAN_DISBURSEMENT':
        return 'Выдача кредита';
      case 'LOAN_REPAYMENT':
        return 'Погашение кредита';
      default:
        return type;
    }
  };

  const getOperationTypeClass = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return 'deposit';
      case 'WITHDRAWAL':
        return 'withdrawal';
      case 'TRANSFER_OUT':
        return 'transfer-out';
      case 'TRANSFER_IN':
        return 'transfer-in';
      case 'LOAN_DISBURSEMENT':
        return 'loan-disbursement';
      case 'LOAN_REPAYMENT':
        return 'loan-repayment';
      default:
        return '';
    }
  };

  const getOperationAmountPrefix = (type) => {
    switch (type) {
      case 'DEPOSIT':
      case 'TRANSFER_IN':
      case 'LOAN_DISBURSEMENT':
        return '+';
      case 'WITHDRAWAL':
      case 'TRANSFER_OUT':
      case 'LOAN_REPAYMENT':
        return '-';
      default:
        return '';
    }
  };

  if (loading) {
    return (
      <div className="history-loading">
        <div className="loading-spinner-large"></div>
      </div>
    );
  }

  return (
    <div className="account-history">
      <div className="history-header">
        <div className="history-header-top">
          <button className="back-button" onClick={() => navigate(`/clients/${clientId}/accounts`)}>
            ← Назад к счетам
          </button>
        </div>
        <h2>История операций по счету</h2>
        <div className="account-id-display">
          {accountId}
        </div>
      </div>

      {operations.length === 0 ? (
        <div className="no-operations">
          <p>По данному счету нет операций</p>
        </div>
      ) : (
        <div className="operations-list">
          {operations.map((operation) => (
            <div key={operation.id} className={`operation-card ${getOperationTypeClass(operation.type)}`}>
              <div className="operation-content">
                <div className="operation-type">
                  Тип операции: <span className={getOperationTypeClass(operation.type)}>
                    {getOperationTypeText(operation.type)}
                  </span>
                </div>
                <div className="operation-amount">
                  Сумма: <span className={getOperationTypeClass(operation.type)}>
                    {getOperationAmountPrefix(operation.type)}{formatCurrency(operation.amount)}
                  </span>
                </div>
                {operation.description && (
                  <div className="operation-description">
                    Описание: {operation.description}
                  </div>
                )}
                <div className="operation-date">
                  Дата: {formatDate(operation.createdAt)}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AccountHistory;