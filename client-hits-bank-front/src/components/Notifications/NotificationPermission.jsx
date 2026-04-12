import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { firebaseService } from '../../api/firebaseService';
import { useAuth } from '../../contexts/AuthContext';
import './NotificationPermission.css';

const NotificationPermission = () => {
  const { user } = useAuth();
  const [show, setShow] = useState(false);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    console.log('NotificationPermission: user =', user);
    
    if (!user) {
      setShow(false);
      setChecked(false);
      return;
    }

    const timer = setTimeout(() => {
      const alreadyAsked = localStorage.getItem('notifications_asked');
      const permissionGranted = Notification.permission === 'granted';
      const permissionDenied = Notification.permission === 'denied';
      
      console.log('Permission status:', { permissionGranted, permissionDenied, alreadyAsked });
      
      if (!permissionGranted && !permissionDenied && !alreadyAsked) {
        setShow(true);
      }
      setChecked(true);
    }, 1000);

    return () => clearTimeout(timer);
  }, [user]);

  const handleEnable = async () => {
    try {
      const success = await firebaseService.setup();
      if (success) {
        localStorage.setItem('notifications_asked', 'true');
        setShow(false);
        toast.success('Уведомления включены');
      }
    } catch (error) {
      toast.error('Не удалось включить уведомления');
    }
  };

  if (!show) return null;

  return (
    <div className="notification-prompt">
      <div className="notification-prompt-content">
        <span>Включите уведомления о новых операциях</span>
        <button onClick={handleEnable} className="enable-btn">Включить</button>
      </div>
    </div>
  );
};

export default NotificationPermission;