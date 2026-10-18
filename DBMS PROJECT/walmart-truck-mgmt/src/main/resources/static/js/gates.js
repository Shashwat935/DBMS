/* gates.js – Warehouse gates live status page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadGates();
    setInterval(loadGates, 5000);
});

/** Load gate overview with active dispatch info */
async function loadGates() {
    try {
        const gates = await api('/api/gates/overview');
        const container = document.getElementById('gatesContainer');
        container.innerHTML = gates.map(g => {
            const statusClass = g.gateStatus.toLowerCase();
            return `
                <div class="col-md-6 col-lg-4">
                    <div class="gate-tile ${statusClass} fade-in">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="gate-number">${g.gateNumber}</span>
                            ${statusBadge(g.gateStatus)}
                        </div>
                        ${g.licensePlate ? `
                            <div class="mb-2">
                                <i class="bi bi-truck"></i> <strong>${g.licensePlate}</strong>
                                <span class="text-muted ms-1">${g.driverName || ''}</span>
                            </div>
                            ${g.isCleaning ? `
                                <div class="alert alert-warning py-1 px-2 mb-2 small">
                                    <i class="bi bi-hourglass-split"></i> Cleaning in progress...
                                </div>` : ''}
                            ${g.operationType ? `
                                <div class="small text-muted mb-1">
                                    <i class="bi bi-box-seam"></i> ${g.operationType}
                                    ${g.loadTypeName ? ` — ${g.loadTypeName}` : ''}
                                    ${g.loadWeight ? ` (${g.loadWeight}t)` : ''}
                                </div>` : ''}
                            ${g.progressPercent != null && g.dispatchStatus === 'IN_PROGRESS' ?
                                progressBar(g.progressPercent) : ''}
                        ` : `
                            <div class="text-muted text-center py-3">
                                <i class="bi bi-inbox" style="font-size:2rem;opacity:0.3"></i>
                                <div class="small mt-1">Gate idle</div>
                            </div>
                        `}
                    </div>
                </div>`;
        }).join('');
    } catch (err) {
        console.error('Gates load error:', err);
    }
}
