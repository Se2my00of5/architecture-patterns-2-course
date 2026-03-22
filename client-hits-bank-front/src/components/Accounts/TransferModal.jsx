import React, { useState } from 'react';
import { toast } from 'react-toastify';

const TransferModal = ({ account, accounts, onClose, onSubmit }) => {
  const [targetAccountId, setTargetAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [transferType, setTransferType] = useState('my');
  const [otherAccountId, setOtherAccountId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const myAccounts = accounts.filter(acc => acc.id !== account.id && acc.status === 'ACTIVE');

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let finalTargetId;
    if (transferType === 'my') {
      if (!targetAccountId) {
        toast.error('Выберите счет получателя');
        return;
      }
      finalTargetId = targetAccountId;
    } else {
      if (!otherAccountId.trim()) {
        toast.error('Введите ID счета получателя');
        return;
      }
      const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
      if (!uuidRegex.test(otherAccountId.trim())) {
        toast.error('Неверный формат ID счета. Используйте формат UUID');
        return;
      }
      finalTargetId = otherAccountId.trim();
    }
    
    if (!amount || Number(amount) <= 0) {
      toast.error('Введите корректную сумму');
      return;
    }
    
    if (Number(amount) > account.balance) {
      toast.error(`Недостаточно средств. Доступно: ${formatCurrency(account.balance, account.currency)}`);
      return;
    }

    setIsSubmitting(true);
    await onSubmit(account.id, finalTargetId, Number(amount), description);
    setIsSubmitting(false);
  };

  const formatCurrency = (value, currency = 'RUB') => {
    const symbols = {
      RUB: '₽',
      USD: '$',
      CNY: '¥'
    };
    return `${Number(value).toLocaleString('ru-RU')} ${symbols[currency] || '₽'}`;
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal transfer-modal">
        <div className="modal-title">Перевод со счета</div>
        <div className="account-info-mini">
          <div>Счет списания: {account.id}</div>
          <div>Доступно: {formatCurrency(account.balance, account.currency)}</div>
          <div>Валюта: {account.currency}</div>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Способ выбора счета</label>
            <div className="radio-group">
              <label className="radio-label">
                <input
                  type="radio"
                  value="my"
                  checked={transferType === 'my'}
                  onChange={() => setTransferType('my')}
                />
                Свой счет
              </label>
              <label className="radio-label">
                <input
                  type="radio"
                  value="other"
                  checked={transferType === 'other'}
                  onChange={() => setTransferType('other')}
                />
                Другой счет
              </label>
            </div>
          </div>

          {transferType === 'my' && (
            <div className="modal-fields">
              <label>Счет получателя</label>
              <select
                value={targetAccountId}
                onChange={(e) => setTargetAccountId(e.target.value)}
                className="currency-select"
                disabled={myAccounts.length === 0}
              >
                <option value="">Выберите счет</option>
                {myAccounts.map(acc => (
                  <option key={acc.id} value={acc.id}>
                    {acc.id.slice(0, 8)}... ({formatCurrency(acc.balance, acc.currency)})
                  </option>
                ))}
              </select>
              {myAccounts.length === 0 && (
                <p className="warning-text">Нет других активных счетов</p>
              )}
            </div>
          )}

          {transferType === 'other' && (
            <div className="modal-fields">
              <label>ID счета получателя</label>
              <input
                type="text"
                value={otherAccountId}
                onChange={(e) => setOtherAccountId(e.target.value)}
                placeholder="Введите ID счета в формате UUID"
              />
              <p className="hint-text">Пример: f47ac10b-58cc-4372-a567-0e02b2c3d479</p>
            </div>
          )}

          <div className="modal-fields">
            <label>Сумма перевода ({account.currency})</label>
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder={`Макс: ${formatCurrency(account.balance, account.currency)}`}
              autoFocus
            />
          </div>

          <div className="modal-fields">
            <label>Описание (необязательно)</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Введите описание перевода"
            />
          </div>

          <div className="create-buttons">
            <button type="button" className="cancel" onClick={onClose}>
              Отмена
            </button>
            <button 
              type="submit" 
              className="save"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Отправка...' : 'Перевести'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default TransferModal;