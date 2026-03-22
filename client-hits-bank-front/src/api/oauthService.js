const USER_SERVICE_URL = 'http://localhost:1115';

const REDIRECT_URI = 'http://localhost:3000/login/oauth2/code/frontend-app';

export const oauthService = {
  initiateLogin() {
    this.generatePKCE().then(({ codeVerifier, codeChallenge }) => {
      const state = Math.random().toString(36).substring(2);
      
      sessionStorage.setItem('oauth_code_verifier', codeVerifier);
      sessionStorage.setItem('oauth_state', state);
      
      const params = new URLSearchParams({
        response_type: 'code',
        client_id: 'frontend-app',
        redirect_uri: REDIRECT_URI,
        scope: 'api',
        code_challenge: codeChallenge,
        code_challenge_method: 'S256',
        state: state
      });
      
      window.location.href = `${USER_SERVICE_URL}/oauth2/authorize?${params.toString()}`;
    });
  },
  
  async generatePKCE() {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    const codeVerifier = btoa(String.fromCharCode(...array))
      .replace(/=/g, '')
      .replace(/\+/g, '-')
      .replace(/\//g, '_');
    
    const encoder = new TextEncoder();
    const data = encoder.encode(codeVerifier);
    const digest = await crypto.subtle.digest('SHA-256', data);
    const codeChallenge = btoa(String.fromCharCode(...new Uint8Array(digest)))
      .replace(/=/g, '')
      .replace(/\+/g, '-')
      .replace(/\//g, '_');
    
    return { codeVerifier, codeChallenge };
  },
  
  async handleCallback(code, state) {
    const savedState = sessionStorage.getItem('oauth_state');
    const codeVerifier = sessionStorage.getItem('oauth_code_verifier');

    if (state !== savedState) {
      throw new Error('State mismatch');
    }
    
    if (!codeVerifier) {
      throw new Error('Code verifier not found');
    }
    
    const params = new URLSearchParams({
      grant_type: 'authorization_code',
      code: code,
      redirect_uri: REDIRECT_URI,
      client_id: 'frontend-app',
      client_secret: 'frontend-app-secret',
      code_verifier: codeVerifier
    });
    
    const response = await fetch(`${USER_SERVICE_URL}/oauth2/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params
    });
    
    const data = await response.json();
    
    if (response.ok) {
      const tokens = {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
        expiresAt: Date.now() + (data.expires_in * 1000),
        tokenType: data.token_type
      };
      
      localStorage.setItem('oauth_tokens', JSON.stringify(tokens));
      
      const userInfo = this.parseJwt(data.access_token);
      
      const roles = userInfo.roles;
      if (!roles || !roles.includes('CLIENT')) {
        this.logout();
        throw new Error('Доступ только для клиентов банка');
      }
      
      localStorage.setItem('user', JSON.stringify({
        id: userInfo.user_id,
        login: userInfo.login,
        fullName: userInfo.full_name,
        roles: userInfo.roles
      }));
      
      sessionStorage.removeItem('oauth_state');
      sessionStorage.removeItem('oauth_code_verifier');
      
      return { success: true, user: userInfo };
    }
    
    return { success: false, error: data.error_description || data.message };
  },

    async refreshToken() {
    const tokens = localStorage.getItem('oauth_tokens');
    if (!tokens) return null;
    
    const { refreshToken } = JSON.parse(tokens);
    if (!refreshToken) return null;
    
    const params = new URLSearchParams({
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
      client_id: 'frontend-app',
      client_secret: 'frontend-app-secret'
    });
    
    try {
      const response = await fetch(`${USER_SERVICE_URL}/oauth2/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      });
      
      const data = await response.json();
      
      if (response.ok) {
        const newTokens = {
          accessToken: data.access_token,
          refreshToken: data.refresh_token || refreshToken,
          expiresAt: Date.now() + (data.expires_in * 1000),
          tokenType: data.token_type
        };
        
        localStorage.setItem('oauth_tokens', JSON.stringify(newTokens));
        return newTokens.accessToken;
      }
      
      this.logout();
      return null;
    } catch (error) {
      console.error('Refresh token error:', error);
      return null;
    }
  },
  
  async ensureValidToken() {
    if (this.isTokenExpired()) {
      return await this.refreshToken();
    }
    return this.getAccessToken();
  },
  
  parseJwt(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      
      return JSON.parse(jsonPayload);
    } catch (e) {
      return {};
    }
  },
  
  getAccessToken() {
      const tokens = localStorage.getItem('oauth_tokens');
      if (!tokens) return null;
      const parsed = JSON.parse(tokens);
      console.log('Access token exists:', !!parsed.accessToken);
      return parsed.accessToken;
  },
  
  isTokenExpired() {
    const tokens = localStorage.getItem('oauth_tokens');
    if (!tokens) return true;
    const { expiresAt } = JSON.parse(tokens);
    return Date.now() >= expiresAt - 60000;
  },
  
  logout() {
    localStorage.removeItem('oauth_tokens');
    localStorage.removeItem('user');
    window.location.href = '/';
  }
};