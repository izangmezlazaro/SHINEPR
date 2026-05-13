// ============================================
// Shine - Auth Integration
// ============================================

(function () {
  function getInitials(name, email) {
    const parts = String(name || '').trim().split(/\s+/).filter(Boolean);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return String(email || 'U').slice(0, 2).toUpperCase();
  }

  function persistUser(usuario, token) {
    if (!usuario) return;

    if (token) {
      localStorage.setItem('shineToken', token);
    }

    localStorage.setItem('shineUserId', String(usuario.id));
    localStorage.setItem('shineUser', JSON.stringify({
      id: usuario.id,
      name: usuario.nombre,
      email: usuario.email,
      initials: getInitials(usuario.nombre, usuario.email),
      role: usuario.rol
    }));

    // Update the global ID used by carrito.js
    window.ID_USUARIO = usuario.id;
  }

  function showError(box, textEl, message) {
    if (textEl) textEl.textContent = message;
    if (box) box.classList.add('show');
  }

  function setupLogin() {
    const form = document.getElementById('loginForm');
    if (!form || !window.ShineAPI) return;

    const emailInput = document.getElementById('loginEmail');
    const passwordInput = document.getElementById('loginPassword');
    const errorBox = document.getElementById('loginError');
    const errorText = document.getElementById('loginErrorText');
    const btn = document.getElementById('loginBtn');

    form.addEventListener('submit', async event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      errorBox?.classList.remove('show');

      if (btn) {
        btn.textContent = 'Signing in…';
        btn.disabled = true;
      }

      try {
        const response = await window.ShineAPI.post('/auth/login', {
          email: emailInput.value.trim(),
          password: passwordInput.value
        });
        persistUser(response.usuario, response.token);
        window.location.href = 'index.html';
      } catch (error) {
        showError(errorBox, errorText, error.message || 'Incorrect email or password.');
        passwordInput.value = '';
        passwordInput.focus();
      } finally {
        if (btn) {
          btn.textContent = 'Sign In';
          btn.disabled = false;
        }
      }
    }, true);
  }

  function setupRegister() {
    const form = document.getElementById('registerForm');
    if (!form || !window.ShineAPI) return;

    const passwordInput = document.getElementById('registerPassword');
    const confirmInput = document.getElementById('confirmPassword');
    const errorBox = document.getElementById('registerError');
    const errorText = document.getElementById('registerErrorText');
    const successBox = document.getElementById('registerSuccess');
    const btn = document.getElementById('registerBtn');

    form.addEventListener('submit', async event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      errorBox?.classList.remove('show');
      successBox?.classList.remove('show');

      const password = passwordInput.value;
      const confirm = confirmInput.value;

      if (password.length < 8) {
        showError(errorBox, errorText, 'Password must be at least 8 characters.');
        return;
      }

      if (password !== confirm) {
        showError(errorBox, errorText, 'Passwords do not match.');
        confirmInput.value = '';
        confirmInput.focus();
        return;
      }

      const firstName = document.getElementById('firstName').value.trim();
      const lastName = document.getElementById('lastName').value.trim();

      if (btn) {
        btn.textContent = 'Creating account…';
        btn.disabled = true;
      }

      try {
        const response = await window.ShineAPI.post('/auth/register', {
          nombre: `${firstName} ${lastName}`.trim(),
          email: document.getElementById('registerEmail').value.trim(),
          password
        });
        persistUser(response.usuario, response.token);
        successBox?.classList.add('show');
        setTimeout(() => {
          window.location.href = 'index.html';
        }, 700);
      } catch (error) {
        showError(errorBox, errorText, error.message || 'Could not create the account.');
      } finally {
        if (btn) {
          btn.textContent = 'Create Account';
          btn.disabled = false;
        }
      }
    }, true);
  }

  function setupLogoutHelpers() {
    window.ShineAuth = {
      persistUser,
      async logout() {
        try {
          if (window.ShineAPI) {
            await window.ShineAPI.post('/auth/logout', {});
          }
        } catch (_) {
          // Ignore errors on logout — always clear local state
        } finally {
          localStorage.removeItem('shineToken');
          localStorage.removeItem('shineUser');
          localStorage.removeItem('shineUserId');
          window.ID_USUARIO = null;
          window.location.href = 'index.html';
        }
      }
    };
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupLogoutHelpers();
    setupLogin();
    setupRegister();
  });
})();
