import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate, useParams } from 'react-router-dom';
import { creditsApi } from '../../api/credits';
import './ClientCredits.css';

const ClientCredits = () => {
  const { clientId } = useParams();
  const navigate = useNavigate();
  const [credits, setCredits] = useState([]);
  const [rating, setRating] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [clientId]);

  const loadData = async () => {
    setLoading(true);
    
    const creditsResult = await creditsApi.getClientCredits(clientId);
    if (creditsResult.success) {
      setCredits(creditsResult.data);
    } else {
      toast.error('Ошибка при загрузке кредитов');
    }
    
    const ratingResult = await creditsApi.getClientRating(clientId);
    if (ratingResult.success) {
      setRating(ratingResult.data);
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
      maximumFractionDigits: 2
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

  const getGradeColor = (grade) => {
    switch (grade) {
      case 'A': return '#28a745';
      case 'B': return '#5cb85c';
      case 'C': return '#ffc107';
      case 'D': return '#fd7e14';
      case 'F': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getGradeText = (grade) => {
    switch (grade) {
      case 'A': return 'Отлично';
      case 'B': return 'Хорошо';
      case 'C': return 'Средне';
      case 'D': return 'Ниже среднего';
      case 'F': return 'Плохо';
      default: return grade;
    }
  };

  if (loading) {
    return (
      <div className="credits-loading">
        <div className="loading-spinner-large"></div>
      </div>
    );
  }

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

      {rating && (
        <div className="rating-card">
          <div className="rating-header">Кредитный рейтинг клиента</div>
          <div className="rating-content">
            <div 
              className="rating-grade" 
              style={{ backgroundColor: getGradeColor(rating.grade) }}
            >
              {rating.grade}
            </div>
            <div className="rating-info">
              <div className="rating-score">Счет: {rating.score}</div>
              <div className="rating-description">{getGradeText(rating.grade)}</div>
              <div className="rating-stats">
                <span>Всего кредитов: {rating.totalCredits}</span>
                <span>Просрочек: {rating.overduePayments}</span>
                <span>Доля своевременных: {rating.onTimePaymentRate}%</span>
              </div>
            </div>
          </div>
        </div>
      )}

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