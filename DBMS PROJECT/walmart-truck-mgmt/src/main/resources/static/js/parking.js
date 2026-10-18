/* parking.js – Parking slot map page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadParkingGrid();
    loadParkingStats();
    loadLoadTypes();
    setInterval(() => {
        loadParkingGrid();
        loadParkingStats();
    }, 5000);
});

/** Load the 4x10 parking grid */
async function loadParkingGrid() {
    try {
        const slots = await api('/api/parking/slots');
        const grid = document.getElementById('parkingGrid');
        grid.innerHTML = slots.map(s => {
            const cls = s.isOccupied ? 'occupied' : 'free';
            const plate = s.licensePlate ? s.licensePlate : '';
            const title = s.isOccupied
                ? `${s.slotNumber}: ${s.licensePlate} (${s.driverName}) [${s.sizeClass}]`
                : `${s.slotNumber}: Empty`;
            return `
                <div class="parking-slot ${cls}" title="${title}"
                     onclick="${s.isOccupied ? `showSlotInfo('${s.licensePlate}','${s.driverName}','${s.sizeClass}','${s.slotNumber}')` : ''}">
                    <div class="slot-label">${s.slotNumber}</div>
                    ${s.isOccupied ? `<div class="slot-plate">${plate}</div>` : ''}
                </div>`;
        }).join('');
    } catch (err) {
        console.error('Parking grid error:', err);
    }
}

/** Load stats counters */
async function loadParkingStats() {
    try {
        const stats = await api('/api/parking/stats');
        document.getElementById('totalSlots').textContent = stats.totalSlots;
        document.getElementById('occupiedSlots').textContent = stats.occupiedSlots;
        document.getElementById('freeSlots').textContent = stats.freeSlots;
    } catch (err) {
        console.error('Stats error:', err);
    }
}

/** Load load types for the dropdown */
async function loadLoadTypes() {
    try {
        const types = await api('/api/load-types');
        const select = document.getElementById('prevLoadType');
        select.innerHTML = '<option value="">None (clean truck)</option>';
        types.forEach(t => {
            select.innerHTML += `<option value="${t.id}">${t.name} (${t.category})</option>`;
        });
    } catch (err) {
        console.error('Load types error:', err);
    }
}

/** Show slot info in an alert or modal */
function showSlotInfo(plate, driver, sizeClass, slot) {
    showToast(`Slot ${slot}: ${plate} | Driver: ${driver} | Size: ${sizeClass}`, 'info');
}

/** Handle truck arrival form */
async function handleArrival(event) {
    event.preventDefault();
    const form = event.target;
    const body = {
        licensePlate: form.licensePlate.value,
        driverName: form.driverName.value,
        originCity: form.originCity.value,
        locationDistance: parseFloat(form.locationDistance.value),
        maxWeightCapacity: parseFloat(form.maxWeightCapacity.value),
        previousLoadTypeId: form.prevLoadType.value ? parseInt(form.prevLoadType.value) : null
    };
    try {
        await api('/api/trucks/arrive', { method: 'POST', body });
        showToast('Truck registered and parked!', 'success');
        form.reset();
        loadParkingGrid();
        loadParkingStats();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}
