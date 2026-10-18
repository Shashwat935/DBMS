/* rules.js – Compatibility matrix page logic */

document.addEventListener('DOMContentLoaded', () => {
    loadCompatibilityMatrix();
});

/** Load all rules and render as a matrix table */
async function loadCompatibilityMatrix() {
    try {
        const rules = await api('/api/compatibility');
        const types = await api('/api/load-types');
        const typeNames = types.map(t => t.name);

        // Build a lookup map: "FROM->TO" => rule
        const ruleMap = {};
        rules.forEach(r => {
            ruleMap[r.fromLoadType + '->' + r.toLoadType] = r;
        });

        // Build the table
        const thead = document.getElementById('matrixHead');
        const tbody = document.getElementById('matrixBody');

        // Header row
        let headerHtml = '<tr><th class="text-start">FROM \\ TO</th>';
        typeNames.forEach(n => { headerHtml += `<th>${n}</th>`; });
        headerHtml += '</tr>';
        thead.innerHTML = headerHtml;

        // Data rows
        let bodyHtml = '';
        typeNames.forEach(from => {
            bodyHtml += `<tr><th class="text-start">${from}</th>`;
            typeNames.forEach(to => {
                if (from === to) {
                    bodyHtml += '<td class="same-type">—</td>';
                } else {
                    const rule = ruleMap[from + '->' + to];
                    if (rule && rule.cleaningRequired) {
                        bodyHtml += `<td class="cleaning-yes">
                            ✓ ${formatCurrency(rule.cleaningCost)}<br>
                            <small>${rule.cleaningMinutes} min</small>
                        </td>`;
                    } else {
                        bodyHtml += '<td class="cleaning-no">No</td>';
                    }
                }
            });
            bodyHtml += '</tr>';
        });
        tbody.innerHTML = bodyHtml;
    } catch (err) {
        console.error('Matrix error:', err);
    }
}
