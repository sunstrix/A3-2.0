/**
 * A3 2.0 - Módulo de Experiência do Usuário (UX)
 * Gerencia: Tema Claro/Escuro, Notificações Toast, Loading Overlay e Interações Mobile
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // ==========================================
    // 🌓 ALTERNÂNCIA DE TEMA (LIGHT/DARK)
    // ==========================================
    const themeToggle = document.getElementById('themeToggle');
    const savedTheme = localStorage.getItem('a3-theme') || 'light';
    
    // Aplica tema salvo ao carregar
    document.documentElement.setAttribute('data-theme', savedTheme);
    if (themeToggle) {
        themeToggle.innerHTML = savedTheme === 'dark' ? '☀️' : '🌙';
        themeToggle.title = savedTheme === 'dark' ? 'Mudar para tema claro' : 'Mudar para tema escuro';
        
        themeToggle.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('a3-theme', newTheme);
            themeToggle.innerHTML = newTheme === 'dark' ? '☀️' : '🌙';
            themeToggle.title = newTheme === 'dark' ? 'Mudar para tema claro' : 'Mudar para tema escuro';
        });
    }

    // ==========================================
    //  SISTEMA DE NOTIFICAÇÕES TOAST
    // ==========================================
    window.showToast = (message, type = 'info', duration = 4000) => {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <div class="toast-title">
                    ${type === 'success' ? '✅ Sucesso' : 
                      type === 'error' ? '❌ Erro' : 
                      type === 'warning' ? '⚠️ Atenção' : 'ℹ️ Informação'}
                </div>
                <div class="toast-message">${message}</div>
            </div>
            <button class="toast-close" aria-label="Fechar">&times;</button>
        `;
        
        container.appendChild(toast);
        
        // Auto-remove após duração
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, duration);

        // Fecha manualmente
        toast.querySelector('.toast-close').addEventListener('click', () => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        });
    };

    // Converte alerts HTML padrão do Bootstrap em Toasts automaticamente
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    if (successAlert) {
        showToast(successAlert.textContent.trim(), 'success');
        successAlert.remove();
    }
    if (errorAlert) {
        showToast(errorAlert.textContent.trim(), 'error');
        errorAlert.remove();
    }

    // ==========================================
    // ⏳ OVERLAY DE CARREGAMENTO (LOADING)
    // ==========================================
    window.showLoading = (message = 'Carregando...') => {
        let overlay = document.getElementById('loadingOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'loadingOverlay';
            overlay.className = 'loading-overlay';
            overlay.innerHTML = `
                <div class="spinner-container">
                    <div class="spinner-border text-primary mb-2" role="status"></div>
                    <p class="mb-0">${message}</p>
                </div>
            `;
            document.body.appendChild(overlay);
        }
        // Pequeno delay para garantir transição CSS
        setTimeout(() => overlay.classList.add('active'), 10);
    };

    window.hideLoading = () => {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) overlay.classList.remove('active');
    };

    // Ativa loading ao submeter formulários (exceto formulários com classe .no-loading)
    document.querySelectorAll('form:not(.no-loading)').forEach(form => {
        form.addEventListener('submit', () => window.showLoading());
    });

    // ==========================================
    // 📱 ENHANCEMENTS MOBILE
    // ==========================================
    // Fecha menu mobile ao clicar em um link
    document.querySelectorAll('.navbar-collapse a').forEach(link => {
        link.addEventListener('click', () => {
            const navbarCollapse = document.querySelector('.navbar-collapse');
            if (navbarCollapse.classList.contains('show')) {
                const bsCollapse = new bootstrap.Collapse(navbarCollapse, { toggle: false });
                bsCollapse.hide();
            }
        });
    });
});