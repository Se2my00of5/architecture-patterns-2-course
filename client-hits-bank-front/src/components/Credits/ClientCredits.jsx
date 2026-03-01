import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { creditsApi } from '../../api/credits';
import { accountsApi } from '../../api/accounts';
import PaymentModal from './PaymentModal';
import './ClientCredits.css';

const ClientCredits = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [credits, setCredits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCredit, setSelectedCredit] = useState(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  useEffect(() => {
    loadCredits();
  }, []);

  const loadCredits = async () => {
    setLoading(true);
    const result = await creditsApi.getClientCredits(user.id);
    if (result.success) {
      setCredits(result.data);
    } else {
      toast.error('Ошибка при загрузке кредитов');
    }
    setLoading(false);
  };

  const handlePayment = async (creditId, accountId, amount) => {
    const result = await creditsApi.makePayment(creditId, accountId, amount);
    
    if (result.success) {
      toast.success('Платеж успешно выполнен');
      setShowPaymentModal(false);
      setSelectedCredit(null);
      loadCredits();
    } else {
      toast.error(result.error || 'Ошибка при выполнении платежа');
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

  const activeCredits = credits.filter(credit => credit.status === 'Active' || credit.status === 'Overdue');
  const closedCredits = credits.filter(credit => credit.status === 'Paid' || credit.status === 'Defaulted');

  return (
    <div className="client-credits">
      <div className="credits-header">
        <div className="credits-header-top">
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
        </div>
        <h2>Ваши кредиты</h2>
      </div>

      {activeCredits.length > 0 && (
        <div className="credits-section">
          <h3>Активные кредиты:</h3>
          <div className="credits-list">
            {activeCredits.map(credit => (
              <div key={credit.id} className="credit-card">
                <div className="credit-info">
                  <div className="credit-id">
                    <span className="label">id:</span> {credit.id}
                  </div>
                  <div className="credit-account">
                    <span className="label">Счет:</span> {credit.accountId}
                  </div>
                  <div className="credit-tariff">
                    <span className="label">Тариф:</span> {credit.tariffName} ({credit.interestRate}%)
                  </div>
                  <div className="credit-amount">
                    <span className="label">Сумма:</span> {formatCurrency(credit.amount)}
                  </div>
                  <div className="credit-remaining">
                    <span className="label">Остаток:</span> {formatCurrency(credit.remainingAmount)}
                  </div>
                  <div className="credit-monthly">
                    <span className="label">Ежемесячный платеж:</span> {formatCurrency(credit.monthlyPayment)}
                  </div>
                  <div className="credit-date">
                    <span className="label">Начат:</span> {formatDate(credit.startDate)}
                  </div>
                  {credit.endDate && (
                    <div className="credit-date">
                      <span className="label">Завершен:</span> {formatDate(credit.endDate)}
                    </div>
                  )}
                  <div className="credit-status">
                    <span className="label">Статус:</span>
                    <span className={`status-badge ${getStatusClass(credit.status)}`}>
                      {getStatusText(credit.status)}
                    </span>
                  </div>
                  {credit.paymentsCount > 0 && (
                    <div className="credit-payments">
                      <span className="label">Платежей:</span> {credit.paymentsCount} (всего {formatCurrency(credit.totalPaid)})
                    </div>
                  )}
                </div>
                
                <div className="credit-actions">
                  <button 
                    className="action-button payment"
                    onClick={() => {
                      setSelectedCredit(credit);
                      setShowPaymentModal(true);
                    }}
                  >
                    Погасить
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {closedCredits.length > 0 && (
        <div className="credits-section">
          <h3>Завершенные кредиты:</h3>
          <div className="credits-list">
            {closedCredits.map(credit => (
              <div key={credit.id} className="credit-card closed">
                <div className="credit-info">
                  <div className="credit-id">
                    <span className="label">id:</span> {credit.id}
                  </div>
                  <div className="credit-tariff">
                    <span className="label">Тариф:</span> {credit.tariffName} ({credit.interestRate}%)
                  </div>
                  <div className="credit-amount">
                    <span className="label">Сумма:</span> {formatCurrency(credit.amount)}
                  </div>
                  <div className="credit-remaining">
                    <span className="label">Остаток:</span> {formatCurrency(credit.remainingAmount)}
                  </div>
                  <div className="credit-date">
                    <span className="label">Начат:</span> {formatDate(credit.startDate)}
                  </div>
                  {credit.endDate && (
                    <div className="credit-date">
                      <span className="label">Завершен:</span> {formatDate(credit.endDate)}
                    </div>
                  )}
                  <div className="credit-status">
                    <span className="label">Статус:</span>
                    <span className={`status-badge ${getStatusClass(credit.status)}`}>
                      {getStatusText(credit.status)}
                    </span>
                  </div>
                  <div className="credit-payments">
                    <span className="label">Платежей:</span> {credit.paymentsCount} (всего {formatCurrency(credit.totalPaid)})
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {credits.length === 0 && (
        <div className="no-credits">
          <p>У вас нет кредитов</p>
        </div>
      )}

      {showPaymentModal && selectedCredit && (
        <PaymentModal
          credit={selectedCredit}
          onClose={() => {
            setShowPaymentModal(false);
            setSelectedCredit(null);
          }}
          onSubmit={handlePayment}
        />
      )}
    </div>
  );
};

export default ClientCredits;