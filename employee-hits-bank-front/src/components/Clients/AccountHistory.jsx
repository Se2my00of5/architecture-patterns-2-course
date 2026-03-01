import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../../api/users';
import './AccountHistory.css';

const AccountHistory = () => {
  const { clientId, accountId } = useParams();
  const navigate = useNavigate();
  const [operations, setOperations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOperations();
  }, [accountId]);

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

  const getOperationTypeText = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return 'Пополнение';
      case 'WITHDRAWAL':
        return 'Снятие';
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
      case 'LOAN_DISBURSEMENT':
        return '+';
      case 'WITHDRAWAL':
      case 'LOAN_REPAYMENT':
        return '-';
      default:
        return '';
    }
  };

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
                    {getOperationAmountPrefix(operation.type)}{operation.amount} ₽
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