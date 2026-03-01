import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { creditsApi } from '../../api/credits.js';
import CreateTariffModal from './CreateTariffModal';
import './Tariffs.css';

const Tariffs = () => {
  const navigate = useNavigate();
  const [tariffs, setTariffs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    loadTariffs();
  }, []);

  const loadTariffs = async () => {
    setLoading(true);
    const result = await creditsApi.getAllTariffs();
    if (result.success) {
      setTariffs(result.data);
    } else {
      toast.error('Ошибка при загрузке тарифов');
    }
    setLoading(false);
  };

  const handleCreateTariff = async (name, interestRate) => {
    const result = await creditsApi.createTariff(name, interestRate);
    
    if (result.success) {
      toast.success('Тариф успешно создан');
      setShowCreateModal(false);
      loadTariffs();
    } else {
      toast.error(result.error || 'Ошибка при создании тарифа');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const activeTariffs = tariffs.filter(t => t.isActive);
  return (
    <div className="tariffs">
      <div className="tariffs-header">
        <div className="tariffs-header-top">
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
          <button 
            className="create-button"
            onClick={() => setShowCreateModal(true)}
          >
            + Новый тариф
          </button>
        </div>
        <h2>Управление тарифами</h2>
      </div>

      {activeTariffs.length > 0 && (
        <div className="tariffs-section">
          <h3>Тарифы:</h3>
          <div className="tariffs-list">
            {activeTariffs.map(tariff => (
              <div key={tariff.id} className="tariff-card">
                <div className="tariff-info">
                  <div className="tariff-id">
                    <span className="label">id:</span> {tariff.id}
                  </div>
                  <div className="tariff-name">
                    <span className="label">Название:</span> {tariff.name}
                  </div>
                  <div className="tariff-rate">
                    <span className="label">Ставка:</span> {tariff.interestRate}%
                  </div>
                  <div className="tariff-date">
                    <span className="label">Создан:</span> {formatDate(tariff.createdAt)}
                  </div>
                  {tariff.updatedAt && (
                    <div className="tariff-date">
                      <span className="label">Изменен:</span> {formatDate(tariff.updatedAt)}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {tariffs.length === 0 && (
        <div className="no-tariffs">
          <p>Тарифы не найдены</p>
        </div>
      )}

      {showCreateModal && (
        <CreateTariffModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateTariff}
        />
      )}
    </div>
  );
};

export default Tariffs;