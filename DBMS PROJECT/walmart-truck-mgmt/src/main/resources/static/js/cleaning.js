/* cleaning.js – Cleaning queue and history page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadActiveCleaning();
    loadCleaningHistory();
    loadCostByTruck();
    setInterval(loadActiveCleaning, 5000);
});

/** Load active cleaning operations */
async function loadActiveCleaning() {
    try {
        const data = await api('/api/cleaning/active');
        const tbody = document.getElementById('activeCleaningBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No active cleaning</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(cl => `
            <tr>
                <td>${cl.id}</td>
                <td>${cl.licensePlate || '—'}</td>
                <td>${cl.previousLoadType || '—'}</td>
                <td>${formatCurrency(cl.cost)}</td>
                <td>${cl.cleaningMinutes || '—'} min</td>
                <td>${formatDate(cl.startTime)}</td>
                <td>
                    <button class="btn btn-sm btn-success" onclick="completeCleaning(${cl.id})">
                        <i class="bi bi-check-lg"></i> Complete
                    </button>
                </td>
            </tr>`).join('');
    } catch (err) {
        console.error('Active cleaning error:', err);
    }
}

/** Load cleaning history */
async function loadCleaningHistory() {
    try {
        const data = await api('/api/cleaning/logs');
        const tbody = document.getElementById('historyBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No cleaning logs</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(cl => `
            <tr>
                <td>${cl.id}</td>
                <td>${cl.licensePlate || '—'}</td>
                <td>${cl.previousLoadType || '—'}</td>
                <td>${formatCurrency(cl.cost)}</td>
                <td>${formatDate(cl.startTime)}</td>
                <td>${formatDate(cl.endTime)}</td>
                <td>${statusBadge(cl.status)}</td>
            </tr>`).join('');
    } catch (err) {
        console.error('History error:', err);
    }
}

/** Load cost by truck (aggregate) */
async function loadCostByTruck() {
    try {
        const minInput = document.getElementById('minCostFilter');
        const minVal = minInput ? (parseFloat(minInput.value) || 0) : 0;
        const data = await api(`/api/cleaning/cost-by-truck?min=${minVal}`);
        const tbody = document.getElementById('costBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td>${r.licensePlate}</td>
                <td>${r.driverName}</td>
                <td>${r.cleaningCount}</td>
                <td>${formatCurrency(r.totalCost)}</td>
            </tr>`).join('');
    } catch (err) {
        console.error('Cost by truck error:', err);
    }
}

/** Complete a cleaning operation */
async function completeCleaning(id) {
    try {
        await api(`/api/cleaning/${id}/complete`, { method: 'POST' });
        showToast('Cleaning completed!', 'success');
        loadActiveCleaning();
        loadCleaningHistory();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}
