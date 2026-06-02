/**
 * Lógica do Dashboard Analítico - A3 2.0
 * Gerencia a renderização de gráficos com suporte dinâmico ao Dark Mode.
 * 
 * Desenvolvido para capturar variáveis CSS e garantir consistência visual.
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // 1. Configurações de Cores Dinâmicas (Captura do :root do CSS)
    const style = getComputedStyle(document.body);
    const textColor = style.getPropertyValue('--text-primary').trim() || '#f8fafc';
    const borderColor = style.getPropertyValue('--border-color').trim() || '#334155';
    const primaryColor = style.getPropertyValue('--primary').trim() || '#6366f1';
    const successColor = style.getPropertyValue('--success').trim() || '#10b981';
    const warningColor = style.getPropertyValue('--warning').trim() || '#f59e0b';
    const dangerColor = style.getPropertyValue('--danger').trim() || '#ef4444';
    const bgPrimary = style.getPropertyValue('--bg-primary').trim() || '#0f172a';

    // 2. Recuperação segura dos dados injetados pelo Thymeleaf
    const statsData = window.dashboardData || { distribuicaoTarefas: {}, projetosPorStatus: {} };

    // Helper para tradução amigável de Enums no gráfico
    const traduzirStatus = (status) => {
        const mapa = {
            'A_FAZER': 'A Fazer',
            'EM_ANDAMENTO': 'Em Andamento',
            'CONCLUIDA': 'Concluída',
            'CANCELADA': 'Cancelada',
            'PLANEJAMENTO': 'Planejamento'
        };
        return mapa[status] || status;
    };

    // --- GRÁFICO 1: DISTRIBUIÇÃO DE TAREFAS (DONUT) ---
    const ctxStatus = document.getElementById('chartStatus');
    if (ctxStatus) {
        const rawLabels = Object.keys(statsData.distribuicaoTarefas);
        const data = Object.values(statsData.distribuicaoTarefas);
        const translatedLabels = rawLabels.map(traduzirStatus);

        new Chart(ctxStatus, {
            type: 'doughnut',
            data: {
                labels: translatedLabels,
                datasets: [{
                    data: data,
                    backgroundColor: [primaryColor, successColor, warningColor, dangerColor, '#94a3b8'],
                    borderWidth: 3,
                    borderColor: bgPrimary,
                    hoverOffset: 15
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { 
                            color: textColor, 
                            padding: 20,
                            font: { family: "'Inter', sans-serif", size: 12, weight: '500' } 
                        }
                    },
                    tooltip: {
                        backgroundColor: bgPrimary,
                        titleColor: textColor,
                        bodyColor: textColor,
                        borderColor: borderColor,
                        borderWidth: 1
                    }
                },
                cutout: '75%'
            }
        });
    }

    // --- GRÁFICO 2: PROJETOS POR STATUS (BARRAS) ---
    const ctxProjetos = document.getElementById('chartProjetos');
    if (ctxProjetos) {
        const rawLabels = Object.keys(statsData.projetosPorStatus);
        const data = Object.values(statsData.projetosPorStatus);
        const translatedLabels = rawLabels.map(traduzirStatus);

        new Chart(ctxProjetos, {
            type: 'bar',
            data: {
                labels: translatedLabels,
                datasets: [{
                    label: 'Projetos',
                    data: data,
                    backgroundColor: primaryColor,
                    borderRadius: 8,
                    barThickness: 40
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: { 
                        grid: { display: false },
                        ticks: { color: textColor, font: { weight: '500' } }
                    },
                    y: { 
                        beginAtZero: true,
                        grid: { color: borderColor, drawTicks: false },
                        ticks: { color: textColor, stepSize: 1 }
                    }
                }
            }
        });
    }
});