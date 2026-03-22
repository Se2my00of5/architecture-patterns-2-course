import React, { useState } from 'react';
import './CreateUserModal.css';

const CreateUserModal = ({ onClose, onSubmit }) => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [roles, setRoles] = useState({
    CLIENT: true,
    EMPLOYEE: false
  });

  const handleRoleChange = (role) => {
    setRoles(prev => ({ ...prev, [role]: !prev[role] }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!login.trim()) {
      alert('Введите логин');
      return;
    }
    
    if (!password) {
      alert('Введите пароль');
      return;
    }
    
    if (!fullName.trim()) {
      alert('Введите ФИО');
      return;
    }
    
    const selectedRoles = Object.entries(roles)
      .filter(([, selected]) => selected)
      .map(([role]) => role);
    
    if (selectedRoles.length === 0) {
      alert('Выберите хотя бы одну роль');
      return;
    }

    onSubmit(login, password, fullName, selectedRoles);
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal">
        <div className="modal-title">Создание нового пользователя</div>
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
            <label>Пароль</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Введите пароль"
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
          <div className="modal-fields">
            <label>Роли</label>
            <div className="checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={roles.CLIENT}
                  onChange={() => handleRoleChange('CLIENT')}
                />
                Клиент
              </label>
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={roles.EMPLOYEE}
                  onChange={() => handleRoleChange('EMPLOYEE')}
                />
                Сотрудник
              </label>
            </div>
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

export default CreateUserModal;