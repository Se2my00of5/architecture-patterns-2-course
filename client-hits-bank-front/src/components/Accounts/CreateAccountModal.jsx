import React, { useState } from 'react';

const CreateAccountModal = ({ onClose, onSubmit }) => {
  const [currency, setCurrency] = useState('RUB');

  const currencies = [
    { code: 'RUB', name: 'Российский рубль', symbol: '₽' },
    { code: 'USD', name: 'Доллар США', symbol: '$' },
    { code: 'CNY', name: 'Китайский юань', symbol: '¥' }
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(currency);
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">Открытие нового счета</div>
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Валюта счета</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="currency-select"
            >
              {currencies.map(curr => (
                <option key={curr.code} value={curr.code}>
                  {curr.name} ({curr.symbol})
                </option>
              ))}
            </select>
          </div>
          <div className="create-buttons">
            <button type="button" className="cancel" onClick={onClose}>
              Отмена
            </button>
            <button type="submit" className="save">
              Открыть счет
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default CreateAccountModal;