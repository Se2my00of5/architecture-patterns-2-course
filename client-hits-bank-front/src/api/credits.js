import apiClient from './client';

const CREDITS_API_URL = 'http://localhost:5005/api/Credit';

export const creditsApi = {
  getClientRating: async (clientId) => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/client/${clientId}/rating`, {
        headers: {
          'accept': 'text/plain'
        }
      });
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении кредитного рейтинга',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении кредитного рейтинга',
        status: 500
      };
    }
  },

  getActiveTariffs: async () => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/tariffs?onlyActive=true`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении тарифов',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении тарифов',
        status: 500
      };
    }
  },

  applyForCredit: async (clientId, accountId, tariffId, amount, termInMonths) => {
    try {
      const response = await apiClient.post(`${CREDITS_API_URL}/apply`, {
        clientId,
        accountId,
        tariffId,
        amount,
        termInMonths
      });
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при оформлении кредита',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при оформлении кредита',
        status: 500
      };
    }
  },

  getClientCredits: async (clientId) => {
    try {
      const response = await apiClient.get(`${CREDITS_API_URL}/client/${clientId}`);
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при получении кредитов',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при получении кредитов',
        status: 500
      };
    }
  },

  makePayment: async (creditId, accountId, amount) => {
    try {
      const response = await apiClient.post(`${CREDITS_API_URL}/payments`, {
        creditId,
        accountId,
        amount
      });
      return { 
        success: true, 
        data: response.data,
        status: response.status 
      };
    } catch (error) {
      if (error.response) {
        return { 
          success: false, 
          error: error.response.data.message || 'Ошибка при платеже',
          status: error.response.status
        };
      }
      return { 
        success: false, 
        error: 'Ошибка при платеже',
        status: 500
      };
    }
  }
};