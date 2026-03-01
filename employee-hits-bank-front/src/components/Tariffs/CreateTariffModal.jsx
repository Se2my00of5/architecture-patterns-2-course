import React, { useState } from 'react';

const CreateTariffModal = ({ onClose, onSubmit }) => {
  const [name, setName] = useState('');
  const [interestRate, setInterestRate] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!name.trim()) {
      alert('Введите название тарифа');
      return;
    }
    
    if (!interestRate || interestRate <= 0 || interestRate > 100) {
      alert('Введите корректную процентную ставку (1-100)');
      return;
    }

    onSubmit(name, Number(interestRate));
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">Создание нового тарифа</div>
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Название тарифа</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
            />
          </div>
          <div className="modal-fields">
            <label>Процентная ставка (%)</label>
            <input
              type="number"
              min="1"
              max="100"
              step="0.1"
              value={interestRate}
              onChange={(e) => setInterestRate(e.target.value)}
            />
          </div>
          <div className="create-buttons">
            <button type="button" className="cancel" onClick={onClose}>
              Отмена
            </button>
            <button type="submit" className="save">
              Создать
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default CreateTariffModal;