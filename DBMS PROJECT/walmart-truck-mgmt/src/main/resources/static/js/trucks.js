/* trucks.js – Trucks management page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadTrucks();
    loadLoadTypes();
    loadLedger();
    setInterval(loadTrucks, 5000);
});

let allTrucks = [];

/** Load all trucks */
async function loadTrucks() {
    try {
        allTrucks = await api('/api/trucks');
        renderTrucks(allTrucks);
    } catch (err) {
        console.error('Load trucks error:', err);
    }
}

/** Render truck table with current filter */
function renderTrucks(trucks) {
    const statusFilter = document.getElementById('statusFilter').value;
    const searchText = document.getElementById('searchPlate').value.toLowerCase();

    let filtered = trucks;
    if (statusFilter) {
        filtered = filtered.filter(t => t.currentStatus === statusFilter);
    }
    if (searchText) {
        filtered = filtered.filter(t =>
            t.licensePlate.toLowerCase().includes(searchText) ||
            t.driverName.toLowerCase().includes(searchText));
    }

    const tbody = document.getElementById('truckTableBody');
    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted">No trucks found</td></tr>';
        return;
    }
    tbody.innerHTML = filtered.map(t => `
        <tr>
            <td>${t.id}</td>
            <td><strong>${t.licensePlate}</strong></td>
            <td>${t.driverName}</td>
            <td>${t.originCity}</td>
            <td>${t.locationDistance} km</td>
            <td>${t.maxWeightCapacity}t <span class="text-muted">(${t.sizeClass})</span></td>
            <td>${t.previousLoadType || '—'}</td>
            <td>${statusBadge(t.currentStatus)}</td>
            <td>${t.slotNumber || '—'}</td>
            <td>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteTruck(${t.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`).join('');
}

/** Filter handlers */
function applyFilter() {
    renderTrucks(allTrucks);
}

/** Load load types for filters */
async function loadLoadTypes() {
    try {
        const types = await api('/api/load-types');
        const select = document.getElementById('addPrevLoadType');
        if (select) {
            select.innerHTML = '<option value="">None</option>';
            types.forEach(t => {
                select.innerHTML += `<option value="${t.id}">${t.name}</option>`;
            });
        }
    } catch (err) {
        console.error('Load types error:', err);
    }
}

/** Load combined ledger (UNION query) */
async function loadLedger() {
    try {
        const data = await api('/api/trucks/ledger');
        const tbody = document.getElementById('ledgerBody');
        if (!tbody) return;
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td>${r.truckId}</td>
                <td>${r.licensePlate}</td>
                <td>${statusBadge(r.currentStatus)}</td>
                <td>${r.locationDistance} km</td>
                <td>${formatDate(r.arrivalTimestamp)}</td>
                <td><span class="badge bg-secondary">${r.source}</span></td>
            </tr>`).join('');
    } catch (err) {
        console.error('Ledger error:', err);
    }
}

/** Delete a truck */
async function deleteTruck(id) {
    if (!confirm('Delete this truck?')) return;
    try {
        await api(`/api/trucks/${id}`, { method: 'DELETE' });
        showToast('Truck deleted', 'success');
        loadTrucks();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

/** Handle add truck form */
async function handleAddTruck(event) {
    event.preventDefault();
    const form = event.target;
    const body = {
        licensePlate: form.licensePlate.value,
        driverName: form.driverName.value,
        originCity: form.originCity.value,
        locationDistance: parseFloat(form.locationDistance.value),
        maxWeightCapacity: parseFloat(form.maxWeightCapacity.value),
        previousLoadTypeId: form.addPrevLoadType.value ? parseInt(form.addPrevLoadType.value) : null
    };
    try {
        await api('/api/trucks/arrive', { method: 'POST', body });
        showToast('Truck registered!', 'success');
        form.reset();
        const modal = bootstrap.Modal.getInstance(document.getElementById('addTruckModal'));
        if (modal) modal.hide();
        loadTrucks();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}
