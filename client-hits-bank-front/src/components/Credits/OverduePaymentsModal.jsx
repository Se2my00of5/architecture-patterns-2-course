import React from 'react';

const OverduePaymentsModal = ({ payments, onClose }) => {
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

  return (
    <>
      <div className="modal-overlay" onClick={onClose}></div>
      <div className="modal overdue-modal">
        <div className="modal-title">Просроченные платежи</div>
        
        {payments.length === 0 ? (
          <p className="no-payments">Нет просроченных платежей</p>
        ) : (
          <div className="payments-list">
            {payments.map(payment => (
              <div key={payment.id} className="payment-item">
                <div className="payment-credit-id">
                  Кредит: {payment.creditId.slice(0, 8)}...
                </div>
                <div className="payment-due-date">
                  Дата платежа: {formatDate(payment.dueDate)}
                </div>
                <div className="payment-status">
                  Статус: {payment.status}
                </div>
                {payment.transactionId && (
                  <div className="payment-transaction">
                    Транзакция: {payment.transactionId.slice(0, 8)}...
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        <div className="create-buttons">
          <button type="button" className="cancel" onClick={onClose}>
            Закрыть
          </button>
        </div>
      </div>
    </>
  );
};

export default OverduePaymentsModal;