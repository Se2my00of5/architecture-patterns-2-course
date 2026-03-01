import React, { useState, useEffect } from 'react';
import { accountsApi } from '../../api/accounts';
import { useAuth } from '../../contexts/AuthContext';

const PaymentModal = ({ credit, onClose, onSubmit }) => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    const result = await accountsApi.getUserAccounts(user.id);
    if (result.success) {
      const activeAccounts = result.data.filter(acc => acc.status === 'ACTIVE');
      setAccounts(activeAccounts);
    }
    setLoading(false);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!selectedAccount) {
      alert('Выберите счет для списания');
      return;
    }
    
    if (!amount || amount <= 0) {
      alert('Введите корректную сумму');
      return;
    }

    if (amount > credit.remainingAmount) {
      alert('Сумма платежа не может превышать остаток по кредиту');
      return;
    }

    onSubmit(credit.id, selectedAccount, Number(amount));
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">Погашение кредита</div>
        
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Счет для списания</label>
            <select
              value={selectedAccount}
              onChange={(e) => setSelectedAccount(e.target.value)}
              disabled={loading || accounts.length === 0}
              className="account-select"
            >
              <option value="">Выберите счет</option>
              {accounts.map(account => (
                <option key={account.id} value={account.id}>
                  {account.id.slice(0, 8)}... (баланс: {formatCurrency(account.balance)})
                </option>
              ))}
            </select>
            {accounts.length === 0 && (
              <p className="warning-text">Нет активных счетов для списания</p>
            )}
          </div>

          <div className="modal-fields">
            <label>Сумма платежа</label>
            <input
              type="number"
              min="1"
              max={credit.remainingAmount}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder={`Макс: ${formatCurrency(credit.remainingAmount)}`}
            />
          </div>

          <div className="create-buttons">
            <button type="button" className="cancel" onClick={onClose}>
              Отмена
            </button>
            <button 
              type="submit" 
              className="save"
              disabled={!selectedAccount || !amount || accounts.length === 0}
            >
              Оплатить
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default PaymentModal;