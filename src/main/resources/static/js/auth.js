let currentUser = null;

function isAllowed(roles) {
    return currentUser !== null && roles.includes(currentUser.role);
}

function showLogin() {
    currentUser = null;
    document.getElementById('app').classList.add('hidden');
    document.getElementById('login').classList.remove('hidden');
}

async function startApp() {
    try {
        currentUser = await api('GET', '/api/auth/me');
    } catch (error) {
        showLogin();
        return;
    }
    document.getElementById('login').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');
    document.getElementById('user-name').textContent = currentUser.fullName;
    document.getElementById('user-role').textContent = currentUser.role === 'ADMIN' ? 'Administrator' : 'Faculty';
    showPage(currentPageKey());
}

document.getElementById('login-form').addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.target;
    const errorBox = document.getElementById('login-error');
    errorBox.classList.add('hidden');
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ username: form.username.value, password: form.password.value })
        });
        if (!response.ok) {
            errorBox.textContent = 'Invalid username or password';
            errorBox.classList.remove('hidden');
            return;
        }
        form.password.value = '';
        await startApp();
    } catch (error) {
        errorBox.textContent = 'Cannot reach the server. Is the application running?';
        errorBox.classList.remove('hidden');
    }
});

document.getElementById('logout').addEventListener('click', async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    showLogin();
});
