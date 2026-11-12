// Centralized API Client & Authentication Module
// Supports same-origin (Spring Boot), Vercel rewrites, and configurable remote endpoints
const API_BASE_URL = window.__API_BASE__ 
    || localStorage.getItem('philately_api_endpoint') 
    || (window.location.protocol === 'file:' ? 'http://localhost:8085' : '');

const API = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const token = localStorage.getItem('philately_token');
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });
            
            // Check for unauthenticated
            if (response.status === 401 && !endpoint.includes('/api/auth/login')) {
                Auth.logout(false);
                return { success: false, message: 'Session expired. Please log in again.' };
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `Error ${response.status}: ${response.statusText}`;
                return { success: false, message, status: response.status, data };
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            return { success: false, message: 'Network or server connection failed. Please ensure the backend is running.' };
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

const Auth = {
    getToken() {
        return localStorage.getItem('philately_token');
    },

    getUser() {
        const userStr = localStorage.getItem('philately_user');
        return userStr ? JSON.parse(userStr) : null;
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    hasRole(role) {
        const user = this.getUser();
        return user && (user.role === role || user.role === `ROLE_${role}`);
    },

    async login(username, password) {
        const res = await API.post('/api/auth/login', { username, password });
        if (res.success && res.data) {
            localStorage.setItem('philately_token', res.data.token);
            localStorage.setItem('philately_user', JSON.stringify(res.data));
            showToast('Welcome back, ' + res.data.fullName + '!', 'success');
            return { success: true, user: res.data };
        } else {
            showToast(res.message || 'Login failed', 'error');
            return { success: false, message: res.message };
        }
    },

    async register(userData) {
        const res = await API.post('/api/auth/register', userData);
        if (res.success && res.data) {
            localStorage.setItem('philately_token', res.data.token);
            localStorage.setItem('philately_user', JSON.stringify(res.data));
            showToast('Registration successful! Your National PDA Account is active.', 'success');
            return { success: true, user: res.data };
        } else {
            showToast(res.message || 'Registration failed', 'error');
            return { success: false, message: res.message };
        }
    },

    logout(redirect = true) {
        localStorage.removeItem('philately_token');
        localStorage.removeItem('philately_user');
        if (redirect) {
            showToast('You have been signed out.', 'info');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 600);
        }
    },

    async refreshProfile() {
        if (!this.isLoggedIn()) return null;
        const res = await API.get('/api/auth/me');
        if (res.success && res.data) {
            const current = this.getUser() || {};
            const updated = { ...current, ...res.data };
            localStorage.setItem('philately_user', JSON.stringify(updated));
            renderHeaderAuth();
            return updated;
        }
        return null;
    }
};

// Toast notification helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'info-circle';
    if (type === 'success') icon = 'check-circle';
    if (type === 'error') icon = 'exclamation-circle';
    if (type === 'warning') icon = 'exclamation-triangle';

    toast.innerHTML = `<i class="fas fa-${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Side Navigation Drawer with Dimmed Backdrop
function toggleNav(forceClose = false) {
    const sideNav = document.getElementById("sideNav");
    let backdrop = document.getElementById("sideNavBackdrop");

    if (!backdrop) {
        backdrop = document.createElement('div');
        backdrop.id = 'sideNavBackdrop';
        backdrop.className = 'side-nav-backdrop';
        backdrop.onclick = () => toggleNav(true);
        document.body.appendChild(backdrop);
    }

    if (!sideNav) return;

    if (forceClose || sideNav.classList.contains('open')) {
        sideNav.classList.remove('open');
        backdrop.classList.remove('active');
    } else {
        sideNav.classList.add('open');
        backdrop.classList.add('active');
    }
}

// Global Nav & Header Auth Synchronization
function renderHeaderAuth() {
    const user = Auth.getUser();
    const navUserArea = document.getElementById('header-user-actions');
    const pdaBalanceBadge = document.getElementById('header-pda-badge');
    const adminLink = document.getElementById('side-nav-admin-link');

    if (user && Auth.isLoggedIn()) {
        if (pdaBalanceBadge) {
            pdaBalanceBadge.style.display = 'inline-flex';
            pdaBalanceBadge.innerHTML = `
                <i class="fas fa-wallet" style="color: #fef08a;"></i>
                <span>PDA: <strong class="badge-val">₹${parseFloat(user.balance || 0).toFixed(2)}</strong></span>
            `;
            pdaBalanceBadge.href = 'Deposit Account.html';
        }
        if (navUserArea) {
            navUserArea.innerHTML = `
                <a href="Deposit Account.html" class="user-status-btn" title="View PDA Dashboard">
                    <i class="fas fa-user-circle"></i>
                    <span>${user.fullName ? user.fullName.split(' ')[0] : user.username}</span>
                </a>
                <button onclick="Auth.logout()" class="btn-outline" style="color: white; border-color: rgba(255,255,255,0.4); padding: 7px 12px; font-size: 0.85rem;" title="Sign Out">
                    <i class="fas fa-sign-out-alt"></i>
                </button>
            `;
        }
        if (adminLink) {
            if (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_OFFICER') {
                adminLink.style.display = 'flex';
            } else {
                adminLink.style.display = 'none';
            }
        }
    } else {
        if (pdaBalanceBadge) {
            pdaBalanceBadge.style.display = 'none';
        }
        if (navUserArea) {
            navUserArea.innerHTML = `
                <a href="login.html" class="user-status-btn">
                    <i class="fas fa-sign-in-alt"></i>
                    <span>Sign In</span>
                </a>
            `;
        }
        if (adminLink) {
            adminLink.style.display = 'none';
        }
    }
}

// Accessibility Font Resizer
let currentFontSize = 16;
function resizeFont(step) {
    if (step === 0) currentFontSize = 16;
    else currentFontSize = Math.min(22, Math.max(13, currentFontSize + step));
    document.documentElement.style.fontSize = currentFontSize + 'px';
}

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    renderHeaderAuth();
    if (Auth.isLoggedIn()) {
        Auth.refreshProfile();
    }
});
