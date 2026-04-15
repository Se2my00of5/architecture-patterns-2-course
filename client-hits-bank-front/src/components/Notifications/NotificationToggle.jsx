import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { firebaseService } from '../../api/firebaseService';
import { useAuth } from '../../contexts/AuthContext';
import './NotificationToggle.css';

const NotificationToggle = () => {
  const { user } = useAuth();
  const [isEnabled, setIsEnabled] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      checkStatus();
    }
  }, [user]);

  const checkStatus = () => {
    const permissionGranted = Notification.permission === 'granted';
    const tokenSaved = localStorage.getItem('fcm_token');
    setIsEnabled(permissionGranted && !!tokenSaved);
  };

  const handleToggle = async () => {
    if (!user) {
      toast.warning('Войдите в систему');
      return;
    }

    setLoading(true);

    try {
      if (isEnabled) {
        await firebaseService.disable();
        setIsEnabled(false);
        toast.success('Уведомления отключены');
      } else {
        const success = await firebaseService.enable();
        if (success) {
          setIsEnabled(true);
          toast.success('Уведомления включены');
        }
      }
    } catch (error) {
      console.error('Toggle error:', error);
      toast.error('Не удалось изменить настройки уведомлений');
    } finally {
      setLoading(false);
    }
  };

  return (
    <button 
      className={`notification-toggle ${isEnabled ? 'enabled' : 'disabled'}`}
      onClick={handleToggle}
      disabled={loading || !user}
    >
      {loading ? (
        <span className="toggle-spinner"></span>
      ) : (
        <>
          <span className="toggle-text">
            {isEnabled ? 'Уведомления включены' : 'Включить уведомления'}
          </span>
        </>
      )}
    </button>
  );
};

export default NotificationToggle;