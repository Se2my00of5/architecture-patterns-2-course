import React, { useState } from 'react';

const AccountOperationsModal = ({ account, type, onClose, onSubmit }) => {
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const numAmount = Number(amount);
    
    if (!amount || numAmount <= 0) {
      alert('Введите корректную сумму');
      return;
    }

    onSubmit(account.id, numAmount, description);
  };

  const titles = {
    deposit: 'Пополнение счета',
    withdraw: 'Снятие со счета'
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">{titles[type]}</div>
        <div className="account-info-mini">
          Счет: {account.id}
        </div>
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Сумма</label>
            <input
              type="number"
              min="1"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="Введите сумму"
              autoFocus
            />
          </div>
          <div className="modal-fields">
            <label>Описание (необязательно)</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Введите описание"
            />
          </div>
          <div className="create-buttons">
            <button type="button" className="cancel" onClick={onClose}>
              Отмена
            </button>
            <button type="submit" className="save">
              {type === 'deposit' ? 'Внести' : 'Снять'}
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default AccountOperationsModal;