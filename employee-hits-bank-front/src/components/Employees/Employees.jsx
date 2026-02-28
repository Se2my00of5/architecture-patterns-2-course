import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { usersApi } from '../../api/users';
import CreateEmployeeModal from './CreateEmployeeModal';
import './Employees.css';

const Employees = () => {
  const navigate = useNavigate();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    loadEmployees();
  }, []);

  const loadEmployees = async () => {
    setLoading(true);
    const result = await usersApi.getEmployees();
    if (result.success) {
      setEmployees(result.data);
    } else {
      toast.error('Ошибка при загрузке списка сотрудников');
    }
    setLoading(false);
  };

  const handleBlockEmployee = async (employee) => {
    const result = await usersApi.block(employee.id);
    
    if (result.success) {
      toast.success('Сотрудник заблокирован');
      loadEmployees();
    } else {
      toast.error(result.error || 'Ошибка при блокировке');
    }
  };

  const handleUnblockEmployee = async (employee) => {
    const result = await usersApi.unblock(employee.id);
    
    if (result.success) {
      toast.success('Сотрудник разблокирован');
      loadEmployees();
    } else {
      toast.error(result.error || 'Ошибка при разблокировке');
    }
  };

  const handleCreateEmployee = async (login, fullName) => {
    const result = await usersApi.createEmployee(login, fullName);
    
    if (result.success) {
      toast.success('Сотрудник успешно создан');
      setShowCreateModal(false);
      loadEmployees();
    } else {
      toast.error(result.error || 'Ошибка при создании сотрудника');
    }
  };

  const activeEmployees = employees.filter(emp => !emp.isBlocked);
  const blockedEmployees = employees.filter(emp => emp.isBlocked);

  return (
    <div className="employees">
      <div className="employees-header">
        <div className="employees-header-top">
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
          <button 
            className="create-button"
            onClick={() => setShowCreateModal(true)}
          >
            + Новый сотрудник
          </button>
        </div>
        <h2>Сотрудники банка</h2>
      </div>

      {activeEmployees.length > 0 && (
        <div className="employees-section">
          <h3>Активные сотрудники:</h3>
          <div className="employees-list">
            {activeEmployees.map(employee => (
              <div key={employee.id} className="employee-card">
                <div className="employee-info">
                  <div className="employee-id">
                    <span className="label">id:</span> {employee.id}
                  </div>
                  <div className="employee-name">
                    <span className="label">ФИО:</span> {employee.fullName}
                  </div>
                </div>
                
                <div className="employee-actions">
                  <button 
                    className="action-button block"
                    onClick={() => handleBlockEmployee(employee)}
                  >
                    Заблокировать
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {blockedEmployees.length > 0 && (
        <div className="employees-section">
          <h3>Заблокированные сотрудники:</h3>
          <div className="employees-list">
            {blockedEmployees.map(employee => (
              <div key={employee.id} className="employee-card blocked">
                <div className="employee-info">
                  <div className="employee-id">
                    <span className="label">id:</span> {employee.id}
                  </div>
                  <div className="employee-name">
                    <span className="label">ФИО:</span> {employee.fullName}
                  </div>
                </div>
                
                <div className="employee-actions">
                  <button 
                    className="action-button unblock"
                    onClick={() => handleUnblockEmployee(employee)}
                  >
                    Разблокировать
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {employees.length === 0 && (
        <div className="no-employees">
          <p>Сотрудники не найдены</p>
        </div>
      )}

      {showCreateModal && (
        <CreateEmployeeModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateEmployee}
        />
      )}
    </div>
  );
};

export default Employees;