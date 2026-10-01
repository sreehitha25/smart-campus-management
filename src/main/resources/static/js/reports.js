(function () {
    const root = document.getElementById('page-reports');

    root.innerHTML =
        '<header class="page-header"><div><h1>Reports</h1>' +
        '<p class="muted">Attendance shortage, top performers and student report cards</p></div></header>' +

        '<div class="panel"><h2>Attendance shortage</h2>' +
        '<div class="toolbar"><label>Students below <input id="threshold" type="number" min="1" max="100" value="75" ' +
        'style="width:80px"> % attendance</label></div><div id="shortage-result"></div></div>' +

        '<div class="panel"><h2>Top performers</h2>' +
        '<div class="toolbar"><select id="top-limit"><option value="5">Top 5</option><option value="10">Top 10</option></select></div>' +
        '<div id="top-result"></div></div>' +

        '<div class="panel"><h2>Student report card</h2>' +
        '<div class="toolbar"><select id="card-student"></select>' +
        '<button id="print-card" class="btn hidden" type="button">Print</button></div>' +
        '<div id="report-card"></div></div>';

    const thresholdEl = document.getElementById('threshold');
    const topLimitEl = document.getElementById('top-limit');
    const cardStudentEl = document.getElementById('card-student');
    const printButton = document.getElementById('print-card');

    function table(headers, rows) {
        return '<table><thead><tr>' + headers.map(h => '<th>' + h + '</th>').join('') + '</tr></thead><tbody>' +
            rows.join('') + '</tbody></table>';
    }

    async function loadShortage() {
        const threshold = Number(thresholdEl.value) || 75;
        const list = await api('GET', '/api/reports/low-attendance?threshold=' + threshold);
        document.getElementById('shortage-result').innerHTML = list.length === 0
            ? '<p class="muted">No students are below ' + threshold + '% attendance.</p>'
            : table(['Student', 'Department', 'Classes held', 'Present', 'Attendance'], list.map(s =>
                '<tr><td>' + esc(s.studentCode) + ' - ' + esc(s.name) + '</td><td>' + esc(s.departmentCode) +
                '</td><td>' + s.totalClasses + '</td><td>' + s.presentClasses + '</td><td>' +
                percentBadge(s.percentage) + '</td></tr>'));
    }

    async function loadTopPerformers() {
        const list = await api('GET', '/api/reports/top-performers?limit=' + topLimitEl.value);
        document.getElementById('top-result').innerHTML = list.length === 0
            ? '<p class="muted">No marks entered yet.</p>'
            : table(['Rank', 'Student', 'Department', 'Courses', 'Average marks'], list.map(t =>
                '<tr><td>' + t.rank + '</td><td>' + esc(t.studentCode) + ' - ' + esc(t.name) + '</td><td>' +
                esc(t.departmentCode) + '</td><td>' + t.coursesCompleted + '</td><td><strong>' +
                t.averageMarks.toFixed(2) + '</strong></td></tr>'));
    }

    async function loadStudents() {
        const previous = cardStudentEl.value;
        const students = await api('GET', '/api/students');
        cardStudentEl.innerHTML = '<option value="">Select student</option>' + students.map(s =>
            '<option value="' + s.id + '"' + (String(s.id) === previous ? ' selected' : '') + '>' +
            esc(s.studentId) + ' - ' + esc(s.name) + '</option>').join('');
    }

    async function loadReportCard() {
        const holder = document.getElementById('report-card');
        if (!cardStudentEl.value) {
            holder.innerHTML = '';
            printButton.classList.add('hidden');
            return;
        }
        const card = await api('GET', '/api/reports/student/' + cardStudentEl.value + '/report-card');
        const rows = card.courses.length === 0
            ? '<tr><td class="empty" colspan="7">No marks entered for this student yet.</td></tr>'
            : card.courses.map(c =>
                '<tr><td>' + esc(c.courseCode) + ' - ' + esc(c.courseName) + '</td><td>' + c.credits + '</td><td>' +
                c.internalMarks + '</td><td>' + c.externalMarks + '</td><td>' + c.totalMarks + '</td><td>' +
                esc(c.grade) + '</td><td>' + c.gradePoint.toFixed(1) + '</td></tr>').join('');
        holder.innerHTML = '<div class="report-card" id="report-card-body">' +
            '<h3>' + esc(card.name) + '</h3>' +
            '<p class="muted">' + esc(card.studentCode) + ' &middot; ' + esc(card.departmentCode) +
            ' &middot; Year ' + card.year + ' &middot; Section ' + esc(card.section) + '</p>' +
            '<div class="panel flush" style="margin-top:14px">' +
            table(['Course', 'Credits', 'Internal', 'External', 'Total', 'Grade', 'Grade point'], [rows]) + '</div>' +
            '<div class="report-summary">' +
            '<div><span class="muted">CGPA</span><strong>' + card.cgpa.toFixed(2) + '</strong></div>' +
            '<div><span class="muted">Total credits</span><strong>' + card.totalCredits + '</strong></div>' +
            '<div><span class="muted">Overall attendance</span><strong>' + card.overallAttendance.toFixed(1) + '%</strong></div>' +
            '</div><p class="muted" style="margin-top:12px">Grade points: A+ 10, A 9, B 8, C 7, D 6, F 0. ' +
            'CGPA = sum(grade point x credits) / total credits.</p></div>';
        
        printButton.classList.remove('hidden');
    }

    thresholdEl.addEventListener('change', () => loadShortage().catch(e => toast(e.message, 'error')));
    topLimitEl.addEventListener('change', () => loadTopPerformers().catch(e => toast(e.message, 'error')));
    cardStudentEl.addEventListener('change', () => loadReportCard().catch(e => toast(e.message, 'error')));
    printButton.addEventListener('click', () => {
        document.body.classList.add('printing-card');
        window.print();
        document.body.classList.remove('printing-card');
    });

    registerPage('reports', async () => {
        await Promise.all([loadShortage(), loadTopPerformers(), loadStudents()]);
        await loadReportCard();
    });
})();
