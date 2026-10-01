const modalEl = document.getElementById('modal');
modalEl.addEventListener('click', event => {
    if (event.target === modalEl) {
        modalEl.classList.add('hidden');
    }
});
document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        modalEl.classList.add('hidden');
    }
});

async function showPage(key) {
    if (!currentUser) {
        return;
    }
    if (!pageHandlers[key]) {
        key = 'dashboard';
    }
    document.querySelectorAll('.page').forEach(p => p.classList.toggle('active', p.id === 'page-' + key));
    document.querySelectorAll('#nav a').forEach(a => a.classList.toggle('active', a.dataset.page === key));
    try {
        await pageHandlers[key]();
    } catch (error) {
        if (error.status !== 401) {
            toast(error.message, 'error');
        }
    }
}

function currentPageKey() {
    return window.location.hash.replace('#', '') || 'dashboard';
}

window.addEventListener('hashchange', () => showPage(currentPageKey()));
startApp();
