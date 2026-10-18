/* reports.js – Reports and analytics page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadWeightByLoadType();
    loadGatePerformance();
    loadSizeClassCounts();
    loadDailySummary();
    loadAboveAverage();
});

/** AGGREGATE 9: Weight by load type */
async function loadWeightByLoadType() {
    try {
        const data = await api('/api/reports/weight-by-load-type');
        const tbody = document.getElementById('weightBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td><strong>${r.loadType}</strong></td>
                <td>${r.dispatchCount}</td>
                <td>${r.totalWeight}t</td>
            </tr>`).join('');
    } catch (err) { console.error(err); }
}

/** AGGREGATE 10: Gate performance */
async function loadGatePerformance() {
    try {
        const data = await api('/api/reports/gate-performance');
        const tbody = document.getElementById('gatePerformanceBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td><strong>${r.gateNumber}</strong></td>
                <td>${r.totalDispatches}</td>
                <td>${r.avgDurationMin != null ? r.avgDurationMin + ' min' : '—'}</td>
            </tr>`).join('');
    } catch (err) { console.error(err); }
}

/** AGGREGATE 11: Size class counts */
async function loadSizeClassCounts() {
    try {
        const data = await api('/api/reports/size-class-counts');
        const tbody = document.getElementById('sizeClassBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td>${statusBadge(r.sizeClass)}</td>
                <td>${r.truckCount}</td>
            </tr>`).join('');
    } catch (err) { console.error(err); }
}

/** AGGREGATE 12: Daily summary */
async function loadDailySummary() {
    try {
        const data = await api('/api/reports/daily-summary');
        const tbody = document.getElementById('dailyBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(r => `
            <tr>
                <td>${r.dispatchDate || '—'}</td>
                <td>${r.dispatchCount}</td>
                <td>${r.totalWeight}t</td>
            </tr>`).join('');
    } catch (err) { console.error(err); }
}

/** SUBQUERY: Above average capacity */
async function loadAboveAverage() {
    try {
        const data = await api('/api/trucks/above-average-capacity');
        const tbody = document.getElementById('aboveAvgBody');
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No data</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(t => `
            <tr>
                <td>${t.licensePlate}</td>
                <td>${t.driverName}</td>
                <td>${t.maxWeightCapacity}t</td>
                <td>${t.sizeClass}</td>
            </tr>`).join('');
    } catch (err) { console.error(err); }
}
