/* ============================================================
   common.js – Shared helper functions for all pages.
   Provides: fetch wrapper, toast messages, date formatting,
   badge rendering, and progress bar helpers.
   ============================================================ */

/**
 * Wrapper for fetch() that handles JSON responses and errors.
 * @param {string} url       The API endpoint URL
 * @param {object} options   Fetch options (method, body, etc.)
 * @returns {Promise<any>}   Parsed JSON response
 */
async function api(url, options = {}) {
    try {
        const defaults = {
            headers: { 'Content-Type': 'application/json' }
        };
        const config = { ...defaults, ...options };
        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }
        const response = await fetch(url, config);
        const text = await response.text();
        const data = text ? JSON.parse(text) : null;

        if (!response.ok) {
            const errMsg = data && data.message ? data.message : `HTTP ${response.status}`;
            throw new Error(errMsg);
        }
        return data;
    } catch (err) {
        console.error(`API error [${url}]:`, err);
        throw err;
    }
}

/**
 * Show a Bootstrap toast notification.
 * @param {string}  message  The message to display
 * @param {string}  type     'success', 'danger', 'warning', 'info'
 */
function showToast(message, type = 'success') {
    // Create toast container if not exists
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(container);
    }

    const icons = {
        success: 'bi-check-circle-fill',
        danger: 'bi-exclamation-triangle-fill',
        warning: 'bi-exclamation-circle-fill',
        info: 'bi-info-circle-fill'
    };

    const id = 'toast-' + Date.now();
    const html = `
        <div id="${id}" class="toast align-items-center text-bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi ${icons[type] || icons.info} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto"
                        data-bs-dismiss="toast"></button>
            </div>
        </div>`;
    container.insertAdjacentHTML('beforeend', html);

    const toastEl = document.getElementById(id);
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();

    // Remove from DOM after hidden
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

/**
 * Format a date/time string for display.
 * @param {string} dateStr  ISO date string or null
 * @returns {string}        Formatted date or '—'
 */
function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit', hour12: true
    });
}

/**
 * Render a status badge.
 * @param {string} status  The status string (e.g. 'PENDING', 'IN_PROGRESS')
 * @returns {string}       HTML for the badge
 */
function statusBadge(status) {
    const label = status ? status.replace(/_/g, ' ') : '—';
    return `<span class="badge badge-status badge-${status}">${label}</span>`;
}

/**
 * Render a progress bar.
 * @param {number} percent   Progress percentage (0-100)
 * @returns {string}         HTML for the progress bar
 */
function progressBar(percent) {
    const p = Math.round(percent || 0);
    let color = 'bg-primary';
    if (p >= 100) color = 'bg-success';
    else if (p >= 60) color = 'bg-info';
    return `
        <div class="progress">
            <div class="progress-bar ${color}" style="width: ${p}%">${p}%</div>
        </div>`;
}

/**
 * Format a number with commas (for currency).
 * @param {number} num
 * @returns {string}
 */
function formatCurrency(num) {
    if (num == null) return '—';
    return '₹' + Number(num).toLocaleString('en-IN');
}

/**
 * Debounce helper – delays function execution.
 * @param {Function} fn     The function to debounce
 * @param {number}   delay  Delay in ms
 * @returns {Function}
 */
function debounce(fn, delay = 300) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}
