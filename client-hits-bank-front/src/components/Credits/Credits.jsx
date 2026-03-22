import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { creditsApi } from '../../api/credits';
import { accountsApi } from '../../api/accounts';
import './Credits.css';

const Credits = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [tariffs, setTariffs] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [rating, setRating] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  
  const [selectedTariff, setSelectedTariff] = useState('');
  const [selectedAccount, setSelectedAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [term, setTerm] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    
    const tariffsResult = await creditsApi.getActiveTariffs();
    if (tariffsResult.success) {
      setTariffs(tariffsResult.data);
    } else {
      toast.error('Ошибка при загрузке тарифов');
    }
    
    const accountsResult = await accountsApi.getUserAccounts(user.id);
    if (accountsResult.success) {
      const activeRubAccounts = accountsResult.data.filter(
        acc => acc.status === 'ACTIVE' && acc.currency === 'RUB'
      );
      setAccounts(activeRubAccounts);
      if (activeRubAccounts.length === 0) {
        toast.warning('У вас нет рублевых счетов для зачисления кредита');
      }
    } else {
      toast.error('Ошибка при загрузке счетов');
    }
    
    const ratingResult = await creditsApi.getClientRating(user.id);
    if (ratingResult.success) {
      setRating(ratingResult.data);
    } else {
      console.log('Rating not available');
    }
    
    setLoading(false);
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedTariff) {
      toast.error('Выберите тариф');
      return;
    }
    
    if (!selectedAccount) {
      toast.error('Выберите счет для зачисления');
      return;
    }
    
    if (!amount || amount <= 0) {
      toast.error('Введите корректную сумму');
      return;
    }
    
    if (!term || term <= 0) {
      toast.error('Введите корректный срок');
      return;
    }

    setSubmitting(true);

    const result = await creditsApi.applyForCredit(
      user.id,
      selectedAccount,
      selectedTariff,
      Number(amount),
      Number(term)
    );
    
    if (result.success) {
      toast.success('Заявка на кредит успешно оформлена!');
    
      setSelectedTariff('');
      setSelectedAccount('');
      setAmount('');
      setTerm('');
    } else {
      toast.error(result.error || 'Ошибка при оформлении кредита');
    }
    
    setSubmitting(false);
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(value);
  };

  if (loading) {
    return (
      <div className="credit-loading">
        <div className="loading-spinner-large"></div>
      </div>
    );
  }

  return (
    <div className="credit-application">
      <div className="credit-header">
        <div className="credit-header-top">
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
        </div>
        <h2>Оформление кредита</h2>
      </div>

      <div className="credit-content">
        <div className="tariffs-section">
          <h3>Доступные тарифы</h3>
          {tariffs.length === 0 ? (
            <p className="no-tariffs">Нет доступных тарифов</p>
          ) : (
            <div className="tariffs-list">
              {tariffs.map(tariff => (
                <div 
                  key={tariff.id} 
                  className={`tariff-card ${selectedTariff === tariff.id ? 'selected' : ''}`}
                  onClick={() => setSelectedTariff(tariff.id)}
                >
                  <div className="tariff-name">{tariff.name}</div>
                  <div className="tariff-rate">
                    Ставка: <span>{tariff.interestRate}%</span>
                  </div>
                  {tariff.createdAt && (
                    <div className="tariff-date">
                      С {new Date(tariff.createdAt).toLocaleDateString('ru-RU')}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="application-form-section">
          <h3>Параметры кредита</h3>
          
          {rating && (
            <div className="rating-card">
              <div className="rating-header">Ваш кредитный рейтинг</div>
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
                  </div>
                </div>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="credit-form">
            <div className="form-group">
              <label>Счет зачисления (только RUB) *</label>
              <select
                value={selectedAccount}
                onChange={(e) => setSelectedAccount(e.target.value)}
                disabled={submitting || accounts.length === 0}
                className="account-select"
              >
                <option value="">Выберите счет</option>
                {accounts.map(account => (
                  <option key={account.id} value={account.id}>
                    {account.id} (баланс: {formatCurrency(account.balance)})
                  </option>
                ))}
              </select>
              {accounts.length === 0 && (
                <p className="warning-text">
                  Нет рублевых счетов. Сначала откройте рублевый счет.
                </p>
              )}
            </div>

            <div className="form-group">
              <label>Сумма кредита (₽)</label>
              <input
                type="number"
                min="1000"
                step="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="Например: 1000000"
                disabled={submitting}
              />
            </div>

            <div className="form-group">
              <label>Срок (месяцев)</label>
              <input
                type="number"
                min="1"
                step="1"
                value={term}
                onChange={(e) => setTerm(e.target.value)}
                placeholder="Например: 12"
                disabled={submitting}
              />
            </div>

            <button 
              type="submit" 
              className="submit-button"
              disabled={!selectedTariff || !selectedAccount || !amount || !term || submitting || accounts.length === 0}
            >
              {submitting ? 'Оформление...' : 'Оформить кредит'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Credits;