import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams } from 'react-router-dom';
import { creditsApi } from '../../api/credits';
import './ClientCredits.css';

const ClientCredits = () => {
  const { clientId } = useParams();
  const navigate = useNavigate();
  const [credits, setCredits] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCredits();
  }, [clientId]);

  const loadCredits = async () => {
    setLoading(true);
    const result = await creditsApi.getClientCredits(clientId);
    if (result.success) {
      setCredits(result.data);
    } else {
      toast.error('Ошибка при загрузке кредитов');
    }
    setLoading(false);
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

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'Active':
        return 'Активный';
      case 'Paid':
        return 'Погашен';
      case 'Overdue':
        return 'Просрочен';
      case 'Defaulted':
        return 'Проблемный';
      default:
        return status;
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'Active':
        return 'status-active';
      case 'Paid':
        return 'status-paid';
      case 'Overdue':
        return 'status-overdue';
      case 'Defaulted':
        return 'status-defaulted';
      default:
        return '';
    }
  };

  return (
    <div className="client-credits">
      <div className="credits-header">
        <div className="credits-header-top">
          <button className="back-button" onClick={() => navigate('/clients')}>
            ← Назад к клиентам
          </button>
        </div>
        <h2>Кредиты клиента</h2>
        <div className="client-id-display">
          ID клиента: {clientId}
        </div>
      </div>

      {credits.length === 0 ? (
        <div className="no-credits">
          <p>У клиента нет кредитов</p>
        </div>
      ) : (
        <div className="credits-list">
          {credits.map(credit => (
            <div key={credit.id} className="credit-card">
              <div className="credit-header-card">
                <span className="credit-id">Кредит: {credit.id}</span>
                <span className={`status-badge ${getStatusClass(credit.status)}`}>
                  {getStatusText(credit.status)}
                </span>
              </div>

              <div className="credit-details-grid">
                <div className="detail-row">
                  <span className="detail-label">Тариф:</span>
                  <span className="detail-value">{credit.tariffName} ({credit.interestRate}%)</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Счет:</span>
                  <span className="detail-value">{credit.accountId}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Сумма кредита:</span>
                  <span className="detail-value amount">{formatCurrency(credit.amount)}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Остаток долга:</span>
                  <span className="detail-value remaining">{formatCurrency(credit.remainingAmount)}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Ежемесячный платеж:</span>
                  <span className="detail-value monthly">{formatCurrency(credit.monthlyPayment)}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Дата начала:</span>
                  <span className="detail-value">{formatDate(credit.startDate)}</span>
                </div>

                {credit.endDate && (
                  <div className="detail-row">
                    <span className="detail-label">Дата завершения:</span>
                    <span className="detail-value">{formatDate(credit.endDate)}</span>
                  </div>
                )}

                <div className="detail-row">
                  <span className="detail-label">Платежей:</span>
                  <span className="detail-value">{credit.paymentsCount}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Всего выплачено:</span>
                  <span className="detail-value">{formatCurrency(credit.totalPaid)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ClientCredits;