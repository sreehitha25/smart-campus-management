/*
 * Reusable list page: server-side paging + sorting, search/filter, add/edit/delete, CSV export.
 * Each module (students.js, courses.js, ...) only describes its columns and form fields.
 */
const pageHandlers = {};

function registerPage(key, onShow) {
    pageHandlers[key] = onShow;
}

function createCrudPage(cfg) {
    const writeRoles = cfg.writeRoles || ['ADMIN'];
    const root = document.getElementById('page-' + cfg.key);
    const modal = document.getElementById('modal');
    const form = document.getElementById('modal-form');
    const optionCache = {};
    const state = { page: 0, size: 10, sortBy: cfg.defaultSort.key, dir: cfg.defaultSort.dir };
    let rows = [];
    let editingId = null;
    let searchTimer;

    root.innerHTML =
        '<header class="page-header">' +
        '<div><h1>' + cfg.title + '</h1><p class="muted" data-role="subtitle"></p></div>' +
        '<button class="btn primary" data-role="add">Add ' + cfg.singular + '</button>' +
        '</header>' +
        '<div class="toolbar">' +
        (cfg.search ? '<input type="search" data-role="search" placeholder="' + cfg.search.placeholder + '">' : '') +
        (cfg.filter ? '<select data-role="filter"></select>' : '') +
        (cfg.exportUrl ? '<a class="btn spacer" href="' + cfg.exportUrl + '" download>Export CSV</a>' : '') +
        '</div>' +
        '<div class="panel flush"><table><thead><tr>' +
        cfg.columns.map(c => '<th' + (c.sort ? ' class="sortable" data-sort="' + c.sort + '"' : '') + '>' +
            c.label + '<span class="arrow"></span></th>').join('') + '<th></th>' +
        '</tr></thead><tbody data-role="rows"></tbody></table>' +
        '<div class="pager"><span data-role="info"></span><div class="pager-controls">' +
        '<select data-role="size"><option>10</option><option>25</option><option>50</option></select>' +
        '<button class="btn small" data-role="prev">Previous</button>' +
        '<span data-role="page-label"></span>' +
        '<button class="btn small" data-role="next">Next</button></div></div></div>';

    const searchEl = root.querySelector('[data-role="search"]');
    const filterEl = root.querySelector('[data-role="filter"]');
    const tbody = root.querySelector('[data-role="rows"]');
    const addButton = root.querySelector('[data-role="add"]');
    const subtitleEl = root.querySelector('[data-role="subtitle"]');
    const prevButton = root.querySelector('[data-role="prev"]');
    const nextButton = root.querySelector('[data-role="next"]');
    const sizeSelect = root.querySelector('[data-role="size"]');

    function canWrite() {
        return isAllowed(writeRoles);
    }

    function applyPermissions() {
        addButton.classList.toggle('hidden', !canWrite());
        subtitleEl.textContent = cfg.subtitle + (canWrite() ? '' : ' (view only for your role)');
    }

    // ---------- dropdown options ----------
    function optionValue(source, item) {
        return source.value ? source.value(item) : item.id;
    }

    function optionsHtml(items, source, emptyLabel, selected) {
        return '<option value="">' + emptyLabel + '</option>' + items.map(item => {
            const value = optionValue(source, item);
            return '<option value="' + esc(value) + '"' + (String(value) === String(selected) ? ' selected' : '') +
                '>' + esc(source.text(item)) + '</option>';
        }).join('');
    }

    async function loadOptionSources() {
        const sources = cfg.fields.filter(f => f.options).map(f => f.options);
        if (cfg.filter) {
            sources.push(cfg.filter.options);
        }
        for (const source of sources) {
            optionCache[source.endpoint] = await api('GET', source.endpoint);
        }
        if (cfg.filter) {
            const previous = filterEl.value;
            filterEl.innerHTML = optionsHtml(optionCache[cfg.filter.options.endpoint], cfg.filter.options,
                cfg.filter.allLabel, previous);
        }
    }

    // ---------- table ----------
    async function loadRows() {
        const params = new URLSearchParams({
            page: state.page, size: state.size, sortBy: state.sortBy, direction: state.dir
        });
        if (filterEl && filterEl.value) {
            params.set(cfg.filter.param, filterEl.value);
        } else if (searchEl && searchEl.value.trim()) {
            params.set('keyword', searchEl.value.trim());
        }
        const result = await api('GET', cfg.endpoint + '/page?' + params.toString());
        if (result.content.length === 0 && result.totalElements > 0 && state.page > 0) {
            state.page = result.totalPages - 1;
            return loadRows();
        }
        rows = result.content;
        renderHeader();
        renderRows();
        renderPager(result);
    }

    function renderHeader() {
        root.querySelectorAll('th.sortable').forEach(th => {
            const arrow = th.querySelector('.arrow');
            arrow.textContent = th.dataset.sort === state.sortBy ? (state.dir === 'asc' ? '\u25B2' : '\u25BC') : '';
        });
    }

    function renderRows() {
        if (rows.length === 0) {
            tbody.innerHTML = '<tr><td class="empty" colspan="' + (cfg.columns.length + 1) + '">' +
                'No ' + cfg.plural + ' found.</td></tr>';
            return;
        }
        const actions = id => canWrite()
            ? '<button class="btn small" data-action="edit" data-id="' + id + '">Edit</button> ' +
              '<button class="btn small danger" data-action="delete" data-id="' + id + '">Delete</button>'
            : '';
        tbody.innerHTML = rows.map(row =>
            '<tr>' + cfg.columns.map(c => '<td>' + (c.html ? c.html(row) : esc(c.value(row))) + '</td>').join('') +
            '<td class="actions">' + actions(row.id) + '</td></tr>').join('');
    }

    function renderPager(result) {
        const from = result.totalElements === 0 ? 0 : result.page * result.size + 1;
        const to = result.page * result.size + result.content.length;
        root.querySelector('[data-role="info"]').textContent =
            'Showing ' + from + '-' + to + ' of ' + result.totalElements;
        root.querySelector('[data-role="page-label"]').textContent =
            'Page ' + (result.totalPages === 0 ? 0 : result.page + 1) + ' of ' + result.totalPages;
        prevButton.disabled = result.page <= 0;
        nextButton.disabled = result.page >= result.totalPages - 1;
    }

    function refresh() {
        loadRows().catch(error => {
            if (error.status !== 401) {
                toast(error.message, 'error');
            }
        });
    }

    // ---------- form ----------
    function fieldHtml(field, value) {
        let control;
        if (field.options) {
            control = '<select name="' + field.name + '">' +
                optionsHtml(optionCache[field.options.endpoint], field.options,
                    'Select ' + field.label.toLowerCase(), value) + '</select>';
        } else if (field.choices) {
            control = '<select name="' + field.name + '">' +
                optionsHtml(field.choices, { value: c => c.value, text: c => c.text },
                    'Select ' + field.label.toLowerCase(), value) + '</select>';
        } else {
            control = '<input name="' + field.name + '" type="' + (field.type || 'text') + '" value="' + esc(value) + '"' +
                (field.placeholder ? ' placeholder="' + esc(field.placeholder) + '"' : '') + '>';
        }
        return '<label class="field"><span>' + field.label + '</span>' + control +
            '<small class="field-error" data-error-for="' + field.name + '"></small></label>';
    }

    function openForm(row) {
        editingId = row ? row.id : null;
        document.getElementById('modal-title').textContent = (row ? 'Edit ' : 'Add ') + cfg.singular;
        form.innerHTML = '<div class="form-error hidden" data-role="form-error"></div>' +
            cfg.fields.map(f => fieldHtml(f, row ? row[f.name] : '')).join('') +
            '<div class="form-actions">' +
            '<button type="button" class="btn" data-role="cancel">Cancel</button>' +
            '<button type="submit" class="btn primary">Save</button></div>';
        form.onsubmit = submitForm;
        modal.classList.remove('hidden');
        const first = form.querySelector('input, select');
        if (first) {
            first.focus();
        }
    }

    function closeForm() {
        modal.classList.add('hidden');
        form.onsubmit = null;
    }

    function readForm() {
        const body = {};
        cfg.fields.forEach(f => {
            let value = form.elements[f.name].value;
            if (value === '') {
                value = null;
            } else if (f.type === 'number' || f.asNumber) {
                value = Number(value);
            }
            body[f.name] = value;
        });
        return body;
    }

    function showFormErrors(error) {
        form.querySelectorAll('.field').forEach(el => el.classList.remove('invalid'));
        form.querySelectorAll('.field-error').forEach(el => el.textContent = '');
        const banner = form.querySelector('[data-role="form-error"]');
        banner.classList.add('hidden');

        const names = Object.keys(error.fieldErrors);
        names.forEach(name => {
            const holder = form.querySelector('[data-error-for="' + name + '"]');
            if (holder) {
                holder.textContent = error.fieldErrors[name];
                holder.parentElement.classList.add('invalid');
            }
        });
        if (names.length === 0) {
            banner.textContent = error.message;
            banner.classList.remove('hidden');
        }
    }

    async function submitForm(event) {
        event.preventDefault();
        try {
            const body = readForm();
            const wasEditing = editingId !== null;
            if (wasEditing) {
                await api('PUT', cfg.endpoint + '/' + editingId, body);
            } else {
                await api('POST', cfg.endpoint, body);
            }
            closeForm();
            toast(cfg.singular + (wasEditing ? ' updated' : ' added') + ' successfully');
            await loadRows();
            if (cfg.afterChange) {
                cfg.afterChange();
            }
        } catch (error) {
            showFormErrors(error);
        }
    }

    async function deleteRow(id) {
        if (!confirm('Delete this ' + cfg.singular.toLowerCase() + '? This cannot be undone.')) {
            return;
        }
        try {
            await api('DELETE', cfg.endpoint + '/' + id);
            toast(cfg.singular + ' deleted');
            await loadRows();
            if (cfg.afterChange) {
                cfg.afterChange();
            }
        } catch (error) {
            toast(error.message, 'error');
        }
    }

    // ---------- events ----------
    addButton.addEventListener('click', () => openForm(null));

    tbody.addEventListener('click', event => {
        const button = event.target.closest('button[data-action]');
        if (!button) {
            return;
        }
        const id = Number(button.dataset.id);
        if (button.dataset.action === 'edit') {
            openForm(rows.find(r => r.id === id));
        } else {
            deleteRow(id);
        }
    });

    form.addEventListener('click', event => {
        if (event.target.matches('[data-role="cancel"]')) {
            closeForm();
        }
    });

    root.querySelector('thead').addEventListener('click', event => {
        const th = event.target.closest('th.sortable');
        if (!th) {
            return;
        }
        if (state.sortBy === th.dataset.sort) {
            state.dir = state.dir === 'asc' ? 'desc' : 'asc';
        } else {
            state.sortBy = th.dataset.sort;
            state.dir = 'asc';
        }
        state.page = 0;
        refresh();
    });

    prevButton.addEventListener('click', () => { state.page = Math.max(state.page - 1, 0); refresh(); });
    nextButton.addEventListener('click', () => { state.page += 1; refresh(); });
    sizeSelect.addEventListener('change', () => { state.size = Number(sizeSelect.value); state.page = 0; refresh(); });

    if (searchEl) {
        searchEl.addEventListener('input', () => {
            if (filterEl) {
                filterEl.value = '';
            }
            state.page = 0;
            clearTimeout(searchTimer);
            searchTimer = setTimeout(refresh, 250);
        });
    }
    if (filterEl) {
        filterEl.addEventListener('change', () => {
            if (searchEl) {
                searchEl.value = '';
            }
            state.page = 0;
            refresh();
        });
    }

    registerPage(cfg.key, async () => {
        applyPermissions();
        await loadOptionSources();
        await loadRows();
        if (cfg.afterShow) {
            await cfg.afterShow();
        }
    });
}
