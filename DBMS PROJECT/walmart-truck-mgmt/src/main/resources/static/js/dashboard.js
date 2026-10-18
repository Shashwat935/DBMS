/* dashboard.js – Dashboard page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    loadGateOverview();
    loadCleaningQueue();
    // Auto-refresh every 5 seconds
    setInterval(() => {
        loadDashboard();
        loadGateOverview();
        loadCleaningQueue();
    }, 5000);
});

/** Load summary cards */
async function loadDashboard() {
    try {
        const data = await api('/api/dashboard/summary');
        document.getElementById('trucksParking').textContent = data.trucksInParking;
        document.getElementById('trucksCleaning').textContent = data.trucksInCleaning;
        document.getElementById('trucksAtGate').textContent = data.trucksAtGate;
        document.getElementById('trucksDispatched').textContent = data.trucksDispatched;
        document.getElementById('gatesIdle').textContent = data.gatesIdle;
        document.getElementById('gatesLoading').textContent = data.gatesLoading;
        document.getElementById('gatesUnloading').textContent = data.gatesUnloading;
        document.getElementById('freeSlots').textContent = data.freeSlots;
        document.getElementById('pendingRequests').textContent = data.pendingRequests;
        document.getElementById('totalCleaningCost').textContent = formatCurrency(data.totalCleaningCost);
    } catch (err) {
        console.error('Dashboard load error:', err);
    }
}

/** Load gate overview tiles */
async function loadGateOverview() {
    try {
        const gates = await api('/api/gates/overview');
        const container = document.getElementById('gateOverview');
        container.innerHTML = gates.map(g => {
            const statusClass = g.gateStatus.toLowerCase();
            const statusLabel = g.gateStatus.replace('_', ' ');
            return `
                <div class="col-md-4 col-lg-2">
                    <div class="gate-tile ${statusClass}">
                        <div class="gate-number">${g.gateNumber}</div>
                        <div class="mt-2">${statusBadge(g.gateStatus)}</div>
                        ${g.licensePlate ? `
                            <div class="mt-2 small">
                                <i class="bi bi-truck"></i> ${g.licensePlate}
                            </div>
                            ${g.isCleaning ? '<span class="badge bg-warning text-dark mt-1 pulse">🧹 Cleaning</span>' : ''}
                        ` : '<div class="mt-2 text-muted small">No truck</div>'}
                    </div>
                </div>`;
        }).join('');
    } catch (err) {
        console.error('Gate overview error:', err);
    }
}

/** Load active cleaning queue */
async function loadCleaningQueue() {
    try {
        const logs = await api('/api/cleaning/active');
        const tbody = document.getElementById('cleaningQueueBody');
        if (logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No active cleaning</td></tr>';
            return;
        }
        tbody.innerHTML = logs.map(cl => `
            <tr>
                <td>${cl.licensePlate || '—'}</td>
                <td>${cl.previousLoadType || '—'}</td>
                <td>${formatCurrency(cl.cost)}</td>
                <td>${formatDate(cl.startTime)}</td>
                <td>${statusBadge(cl.status)}</td>
            </tr>`).join('');
    } catch (err) {
        console.error('Cleaning queue error:', err);
    }
}

/** Run auto-assign for all pending dispatches */
async function runAutoAssign() {
    try {
        const results = await api('/api/dispatch/auto-assign', { method: 'POST' });
        if (results.length === 0) {
            showToast('No pending requests to assign', 'info');
        } else {
            results.forEach(r => showToast(r.message, r.success ? 'success' : 'warning'));
        }
        loadDashboard();
        loadGateOverview();
        loadCleaningQueue();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}
