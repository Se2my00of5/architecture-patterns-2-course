import React, { useState } from 'react';

const CreateClientModal = ({ onClose, onSubmit }) => {
  const [login, setLogin] = useState('');
  const [fullName, setFullName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!login.trim() || !fullName.trim()) {
      alert('Заполните все поля');
      return;
    }

    onSubmit(login, fullName);
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">Создание нового клиента</div>
        <form onSubmit={handleSubmit}>
          <div className="modal-fields">
            <label>Логин</label>
            <input
              type="text"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
              placeholder="Введите логин"
              autoFocus
            />
          </div>
          <div className="modal-fields">
            <label>ФИО</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Введите ФИО"
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

export default CreateClientModal;