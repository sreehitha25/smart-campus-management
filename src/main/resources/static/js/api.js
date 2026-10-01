class ApiError extends Error {
    constructor(message, status, fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors || {};
    }
}

async function api(method, url, body) {
    let response;
    try {
        response = await fetch(url, {
            method,
            headers: body ? { 'Content-Type': 'application/json' } : {},
            body: body ? JSON.stringify(body) : undefined
        });
    } catch (networkError) {
        throw new ApiError('Cannot reach the server. Is the application running?', 0);
    }

    if (response.status === 401 && !url.startsWith('/api/auth/')) {
        showLogin();
    }
    if (response.status === 204) {
        return null;
    }
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const message = data && data.message ? data.message : 'Request failed (' + response.status + ')';
        throw new ApiError(message, response.status, data && data.errors);
    }
    return data;
}

function esc(value) {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

let toastTimer;
function toast(message, type) {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.className = 'toast' + (type === 'error' ? ' error' : '');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.add('hidden'), 3500);
}

function percentBadge(percentage) {
    const cls = percentage >= 75 ? 'ok' : (percentage >= 60 ? 'warn' : 'bad');
    return '<span class="badge ' + cls + '">' + percentage.toFixed(1) + '%</span>';
}
