const CONFIG_KEY = 'auth_test_config';
const TOKENS_KEY = 'auth_test_tokens';
const DEFAULT_CONFIG = {
  baseUrl: 'http://localhost:1115',
  clientId: 'frontend-app',
  clientSecret: 'frontend-app-secret',
  redirectUri: 'http://localhost:3000/login/oauth2/code/frontend-app',
  scope: 'api'
};

const cfgEls = {
  baseUrl: document.getElementById('baseUrl'),
  clientId: document.getElementById('clientId'),
  clientSecret: document.getElementById('clientSecret'),
  redirectUri: document.getElementById('redirectUri'),
  scope: document.getElementById('scope')
};

const statusEl = document.getElementById('status');
const tokensOut = document.getElementById('tokensOut');
const claimsOut = document.getElementById('claimsOut');
const apiOut = document.getElementById('apiOut');

function setStatus(text, isError = false) {
  statusEl.textContent = text;
  statusEl.className = isError ? 'err' : 'ok';
}

function saveConfig() {
  const config = {
    baseUrl: cfgEls.baseUrl.value.trim(),
    clientId: cfgEls.clientId.value.trim(),
    clientSecret: cfgEls.clientSecret.value.trim(),
    redirectUri: cfgEls.redirectUri.value.trim(),
    scope: cfgEls.scope.value.trim()
  };
  localStorage.setItem(CONFIG_KEY, JSON.stringify(config));
  return config;
}

function loadConfig() {
  const saved = localStorage.getItem(CONFIG_KEY);
  if (!saved) {
    cfgEls.baseUrl.value = DEFAULT_CONFIG.baseUrl;
    cfgEls.clientId.value = DEFAULT_CONFIG.clientId;
    cfgEls.clientSecret.value = DEFAULT_CONFIG.clientSecret;
    cfgEls.redirectUri.value = DEFAULT_CONFIG.redirectUri;
    cfgEls.scope.value = DEFAULT_CONFIG.scope;
    return saveConfig();
  }

  const config = {
    ...DEFAULT_CONFIG,
    ...JSON.parse(saved)
  };

  const isLegacyTestClient = config.clientId === 'frontend-test';
  if (isLegacyTestClient) {
    config.clientId = DEFAULT_CONFIG.clientId;
    config.clientSecret = DEFAULT_CONFIG.clientSecret;
    config.redirectUri = DEFAULT_CONFIG.redirectUri;
  }

  if (config.clientId === DEFAULT_CONFIG.clientId && !config.clientSecret) {
    config.clientSecret = DEFAULT_CONFIG.clientSecret;
  }

  cfgEls.baseUrl.value = config.baseUrl || '';
  cfgEls.clientId.value = config.clientId || '';
  cfgEls.clientSecret.value = config.clientSecret || '';
  cfgEls.redirectUri.value = config.redirectUri || '';
  cfgEls.scope.value = config.scope || '';

  localStorage.setItem(CONFIG_KEY, JSON.stringify(config));
  return config;
}

function saveTokens(tokens) {
  localStorage.setItem(TOKENS_KEY, JSON.stringify(tokens));
}

function getTokens() {
  const raw = localStorage.getItem(TOKENS_KEY);
  return raw ? JSON.parse(raw) : null;
}

function normalizeTokenPayload(payload) {
  if (!payload || typeof payload !== 'object') {
    return payload;
  }

  const accessToken = payload.access_token || payload.accessToken || null;
  const refreshToken = payload.refresh_token || payload.refreshToken || null;

  return {
    ...payload,
    access_token: accessToken,
    refresh_token: refreshToken
  };
}

function clearTokens() {
  localStorage.removeItem(TOKENS_KEY);
}

function base64UrlToString(input) {
  const normalized = input.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
  return atob(padded);
}

function decodeJwt(token) {
  if (!token || token.split('.').length < 2) return null;
  try {
    const payload = token.split('.')[1];
    return JSON.parse(base64UrlToString(payload));
  } catch {
    return null;
  }
}

async function sha256base64url(plain) {
  const data = new TextEncoder().encode(plain);
  const hash = await crypto.subtle.digest('SHA-256', data);
  const bytes = new Uint8Array(hash);
  let binary = '';
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function randomString(length = 64) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
  const randomValues = new Uint32Array(length);
  crypto.getRandomValues(randomValues);
  let result = '';
  for (let i = 0; i < length; i += 1) {
    result += chars[randomValues[i] % chars.length];
  }
  return result;
}

function renderTokenInfo() {
  const tokens = normalizeTokenPayload(getTokens());
  if (!tokens) {
    tokensOut.textContent = '(пусто)';
    claimsOut.textContent = '(пусто)';
    return;
  }

  tokensOut.textContent = JSON.stringify(tokens, null, 2);
  claimsOut.textContent = JSON.stringify(decodeJwt(tokens.access_token), null, 2);
}

async function startAuth() {
  try {
    const cfg = saveConfig();
    const codeVerifier = randomString(86);
    const codeChallenge = await sha256base64url(codeVerifier);
    const state = randomString(24);

    sessionStorage.setItem('pkce_verifier', codeVerifier);
    sessionStorage.setItem('oauth_state', state);

    const params = new URLSearchParams({
      response_type: 'code',
      client_id: cfg.clientId,
      redirect_uri: cfg.redirectUri,
      scope: cfg.scope,
      code_challenge: codeChallenge,
      code_challenge_method: 'S256',
      state
    });

    window.location.href = `${cfg.baseUrl}/oauth2/authorize?${params.toString()}`;
  } catch (error) {
    setStatus(`Ошибка старта авторизации: ${error.message}`, true);
  }
}

async function exchangeCode(code, state) {
  const expectedState = sessionStorage.getItem('oauth_state');
  if (!expectedState || expectedState !== state) {
    throw new Error('Неверный state');
  }

  const codeVerifier = sessionStorage.getItem('pkce_verifier');
  if (!codeVerifier) {
    throw new Error('PKCE verifier не найден');
  }

  const cfg = loadConfig();
  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code,
    redirect_uri: cfg.redirectUri,
    client_id: cfg.clientId,
    client_secret: cfg.clientSecret,
    code_verifier: codeVerifier
  });

  const response = await fetch(`${cfg.baseUrl}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body
  });

  const data = await response.json();
  if (!response.ok) {
    if (data.error === 'invalid_client') {
      throw new Error('invalid_client: проверь client_id/client_secret, затем нажми "Сохранить конфиг" и повтори вход');
    }
    throw new Error(data.error_description || data.error || `HTTP ${response.status}`);
  }

  saveTokens(normalizeTokenPayload(data));
  sessionStorage.removeItem('pkce_verifier');
  sessionStorage.removeItem('oauth_state');

  const cleanUrl = new URL(window.location.href);
  cleanUrl.searchParams.delete('code');
  cleanUrl.searchParams.delete('state');
  window.history.replaceState({}, document.title, cleanUrl.toString());

  if (getTokens()?.refresh_token) {
    setStatus('Успешный логин, access_token и refresh_token получены');
  } else {
    setStatus('Успешный логин, но refresh_token не выдан сервером', true);
  }
  renderTokenInfo();
}

async function refreshToken() {
  const tokens = normalizeTokenPayload(getTokens());
  if (!tokens?.refresh_token) {
    setStatus('Нет refresh_token в сохраненных токенах', true);
    return;
  }

  const cfg = saveConfig();
  const body = new URLSearchParams({
    grant_type: 'refresh_token',
    refresh_token: tokens.refresh_token,
    client_id: cfg.clientId,
    client_secret: cfg.clientSecret
  });

  const response = await fetch(`${cfg.baseUrl}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body
  });

  const data = await response.json();
  if (!response.ok) {
    if (data.error === 'invalid_client') {
      throw new Error('invalid_client: проверь client_id/client_secret, затем нажми "Сохранить конфиг"');
    }
    throw new Error(data.error_description || data.error || `HTTP ${response.status}`);
  }

  const normalized = normalizeTokenPayload(data);

  saveTokens({
    ...tokens,
    ...normalized,
    refresh_token: normalized.refresh_token || tokens.refresh_token
  });

  setStatus('Токен обновлён');
  renderTokenInfo();
}

async function callMe() {
  const tokens = normalizeTokenPayload(getTokens());
  if (!tokens?.access_token) {
    setStatus('Сначала получите access_token', true);
    return;
  }

  const cfg = saveConfig();
  const response = await fetch(`${cfg.baseUrl}/api/auth/me`, {
    headers: {
      Authorization: `Bearer ${tokens.access_token}`
    }
  });

  const text = await response.text();
  let payload;

  try {
    payload = JSON.parse(text);
  } catch {
    payload = text;
  }

  apiOut.textContent = JSON.stringify(
    {
      status: response.status,
      ok: response.ok,
      body: payload
    },
    null,
    2
  );

  if (!response.ok) {
    setStatus('Запрос /api/auth/me завершился с ошибкой', true);
    return;
  }

  setStatus('Запрос /api/auth/me успешен');
}

function initFromUrl() {
  const url = new URL(window.location.href);
  const code = url.searchParams.get('code');
  const state = url.searchParams.get('state');
  const error = url.searchParams.get('error');

  if (error) {
    setStatus(`Ошибка авторизации: ${error}`, true);
    return;
  }

  if (code && state) {
    exchangeCode(code, state).catch((e) => {
      setStatus(`Не удалось обменять code на токен: ${e.message}`, true);
    });
  }
}

document.getElementById('saveCfg').addEventListener('click', () => {
  saveConfig();
  setStatus('Конфиг сохранён');
});

document.getElementById('authBtn').addEventListener('click', startAuth);

document.getElementById('clearBtn').addEventListener('click', () => {
  clearTokens();
  apiOut.textContent = '(пусто)';
  renderTokenInfo();
  setStatus('Токены очищены');
});

document.getElementById('refreshBtn').addEventListener('click', () => {
  refreshToken().catch((e) => setStatus(`Ошибка refresh: ${e.message}`, true));
});

document.getElementById('meBtn').addEventListener('click', () => {
  callMe().catch((e) => setStatus(`Ошибка /api/auth/me: ${e.message}`, true));
});

loadConfig();
renderTokenInfo();
initFromUrl();
