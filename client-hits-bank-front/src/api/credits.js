import axios from 'axios';

const CREDITS_API_URL = 'http://localhost:5005/api/Credit';

export const creditsApi = {
  getActiveTariffs: async () => {
    try {
      const response = await axios.get(`${CREDITS_API_URL}/tariffs?onlyActive=true`, {
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
      const response = await axios.post(
        `${CREDITS_API_URL}/apply`,
        {
          clientId,
          accountId,
          tariffId,
          amount,
          termInMonths
        },
        {
          headers: {
            'accept': 'text/plain',
            'Content-Type': 'application/json'
          }
        }
      );
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
      const response = await axios.get(`${CREDITS_API_URL}/client/${clientId}`, {
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
      const response = await axios.post(
        `${CREDITS_API_URL}/payments`,
        {
          creditId,
          accountId,
          amount
        },
        {
          headers: {
            'accept': 'text/plain',
            'Content-Type': 'application/json'
          }
        }
      );
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
  },
};