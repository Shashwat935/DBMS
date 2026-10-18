/* dispatch.js – Dispatch operations page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadLoadTypes();
    loadPending();
    loadActive();
    setInterval(() => {
        loadPending();
        loadActive();
    }, 5000);
});

/** Load load types for the form dropdown */
async function loadLoadTypes() {
    try {
        const types = await api('/api/load-types');
        const select = document.getElementById('loadTypeSelect');
        select.innerHTML = '<option value="">Select load type...</option>';
        types.forEach(t => {
            select.innerHTML += `<option value="${t.id}">${t.name} (${t.category})</option>`;
        });
    } catch (err) {
        console.error('Load types error:', err);
    }
}

/** Load pending dispatches */
async function loadPending() {
    try {
        const data = await api('/api/dispatch/pending');
        const tbody = document.getElementById('pendingBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No pending requests</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(d => `
            <tr>
                <td>${d.id}</td>
                <td>${d.loadTypeName}</td>
                <td>${d.operationType}</td>
                <td>${d.loadWeight}t</td>
                <td>${d.destinationDistance} km</td>
                <td>${d.isEmergency ? '<span class="badge badge-emergency">EMERGENCY</span>' : 'No'}</td>
                <td>${formatDate(d.requestedAt)}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="assignDispatch(${d.id})">
                        <i class="bi bi-check2-circle"></i> Assign
                    </button>
                </td>
            </tr>`).join('');
    } catch (err) {
        console.error('Load pending error:', err);
    }
}

/** Load active dispatches */
async function loadActive() {
    try {
        const data = await api('/api/dispatch/active');
        const tbody = document.getElementById('activeBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">No active operations</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(d => `
            <tr>
                <td>${d.id}</td>
                <td>${d.truckPlate || '—'}</td>
                <td>${d.gateNumber || '—'}</td>
                <td>${d.loadTypeName}</td>
                <td>${d.operationType}</td>
                <td>${d.loadWeight}t</td>
                <td>${statusBadge(d.status)}</td>
                <td style="min-width:140px">${progressBar(d.progressPercent)}</td>
                <td>
                    ${d.status === 'IN_PROGRESS' ? `
                        <button class="btn btn-sm btn-success me-1" onclick="completeDispatch(${d.id})">
                            <i class="bi bi-check-lg"></i> Complete
                        </button>` : ''}
                    ${d.status !== 'COMPLETED' && d.status !== 'CANCELLED' ? `
                        <button class="btn btn-sm btn-outline-danger" onclick="cancelDispatch(${d.id})">
                            <i class="bi bi-x-lg"></i> Cancel
                        </button>` : ''}
                </td>
            </tr>`).join('');
    } catch (err) {
        console.error('Load active error:', err);
    }
}

/** Submit a new dispatch request */
async function handleDispatchRequest(event) {
    event.preventDefault();
    const form = event.target;
    const body = {
        loadTypeId: parseInt(form.loadTypeSelect.value),
        loadWeight: parseFloat(form.loadWeight.value),
        operationType: form.operationType.value,
        destinationDistance: parseFloat(form.destinationDistance.value) || 0,
        isEmergency: form.isEmergency.checked
    };
    try {
        await api('/api/dispatch/request', { method: 'POST', body });
        showToast('Dispatch request created!', 'success');
        form.reset();
        loadPending();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

/** Assign a single dispatch */
async function assignDispatch(id) {
    try {
        await api(`/api/dispatch/${id}/assign`, { method: 'POST' });
        showToast('Dispatch assigned!', 'success');
        loadPending();
        loadActive();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

/** Complete a dispatch */
async function completeDispatch(id) {
    try {
        await api(`/api/dispatch/${id}/complete`, { method: 'POST' });
        showToast('Dispatch completed!', 'success');
        loadActive();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

/** Cancel a dispatch */
async function cancelDispatch(id) {
    if (!confirm('Cancel this dispatch?')) return;
    try {
        await api(`/api/dispatch/${id}/cancel`, { method: 'POST' });
        showToast('Dispatch cancelled', 'warning');
        loadPending();
        loadActive();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}
