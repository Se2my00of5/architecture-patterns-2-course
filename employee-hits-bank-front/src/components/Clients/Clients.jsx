import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { usersApi } from '../../api/users';
import CreateClientModal from './CreateClientModal';
import './Clients.css';

const Clients = () => {
  const navigate = useNavigate();
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    loadClients();
  }, []);

  const loadClients = async () => {
    setLoading(true);
    const result = await usersApi.getClients();
    if (result.success) {
      setClients(result.data);
    } else {
      toast.error('Ошибка при загрузке списка клиентов');
    }
    setLoading(false);
  };

  const handleBlockClient = async (client) => {
    const result = await usersApi.block(client.id);
    
    if (result.success) {
      toast.success('Клиент заблокирован');
      loadClients();
    } else {
      toast.error(result.error || 'Ошибка при блокировке');
    }
  };

  const handleUnblockClient = async (client) => {
    const result = await usersApi.unblock(client.id);
    
    if (result.success) {
      toast.success('Клиент разблокирован');
      loadClients();
    } else {
      toast.error(result.error || 'Ошибка при разблокировке');
    }
  };

  const handleCreateClient = async (login, fullName) => {
    const result = await usersApi.createClient(login, fullName);
    
    if (result.success) {
      toast.success('Клиент успешно создан');
      setShowCreateModal(false);
      loadClients();
    } else {
      toast.error(result.error || 'Ошибка при создании клиента');
    }
  };

  const handleViewAccounts = (clientId) => {
    navigate(`/clients/${clientId}/accounts`);
  };

  const activeClients = clients.filter(client => !client.isBlocked);
  const blockedClients = clients.filter(client => client.isBlocked);

  return (
    <div className="clients">
      <div className="clients-header">
        <div className="clients-header-top">
          <button className="back-button" onClick={() => navigate('/dashboard')}>
            ← Назад
          </button>
          <button 
            className="create-button"
            onClick={() => setShowCreateModal(true)}
          >
            + Новый клиент
          </button>
        </div>
        <h2>Клиенты банка</h2>
      </div>

      {activeClients.length > 0 && (
        <div className="clients-section">
          <h3>Активные клиенты:</h3>
          <div className="clients-list">
            {activeClients.map(client => (
              <div key={client.id} className="client-card">
                <div className="client-info">
                  <div className="client-id">
                    <span className="label">id:</span> {client.id}
                  </div>
                  <div className="client-name">
                    <span className="label">ФИО:</span> {client.fullName}
                  </div>
                </div>
                
                <div className="client-actions">
                  <button 
                    className="action-button accounts"
                    onClick={() => handleViewAccounts(client.id)}
                  >
                    Счета
                  </button>
                  <button 
                    className="action-button block"
                    onClick={() => handleBlockClient(client)}
                  >
                    Заблокировать
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {blockedClients.length > 0 && (
        <div className="clients-section">
          <h3>Заблокированные клиенты:</h3>
          <div className="clients-list">
            {blockedClients.map(client => (
              <div key={client.id} className="client-card blocked">
                <div className="client-info">
                  <div className="client-id">
                    <span className="label">id:</span> {client.id}
                  </div>
                  <div className="client-name">
                    <span className="label">ФИО:</span> {client.fullName}
                  </div>
                </div>
                
                <div className="client-actions">
                  <button 
                    className="action-button accounts"
                    onClick={() => handleViewAccounts(client.id)}
                  >
                    Счета
                  </button>
                  <button 
                    className="action-button unblock"
                    onClick={() => handleUnblockClient(client)}
                  >
                    Разблокировать
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {clients.length === 0 && (
        <div className="no-clients">
          <p>Клиенты не найдены</p>
        </div>
      )}

      {showCreateModal && (
        <CreateClientModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateClient}
        />
      )}
    </div>
  );
};

export default Clients;