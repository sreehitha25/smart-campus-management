(function () {
    const studentOption = { endpoint: '/api/students', text: s => s.studentId + ' - ' + s.name };
    const courseOption = { endpoint: '/api/courses', text: c => c.courseCode + ' - ' + c.courseName };

    async function loadSummary() {
        const studentId = document.getElementById('summary-student').value;
        const result = document.getElementById('summary-result');
        if (!studentId) {
            result.innerHTML = '<p class="muted">Choose a student to see attendance for each course.</p>';
            return;
        }
        try {
            const summary = await api('GET', '/api/attendance/student/' + studentId + '/summary');
            if (summary.length === 0) {
                result.innerHTML = '<p class="muted">No attendance has been marked for this student yet.</p>';
                return;
            }
            result.innerHTML = '<table><thead><tr><th>Course</th><th>Classes held</th><th>Present</th>' +
                '<th>Absent</th><th>Attendance</th></tr></thead><tbody>' +
                summary.map(s => '<tr><td>' + esc(s.courseCode) + ' - ' + esc(s.courseName) + '</td><td>' +
                    s.totalClasses + '</td><td>' + s.presentClasses + '</td><td>' + s.absentClasses +
                    '</td><td>' + percentBadge(s.percentage) + '</td></tr>').join('') +
                '</tbody></table>';
        } catch (error) {
            toast(error.message, 'error');
        }
    }

    createCrudPage({
        key: 'attendance',
        title: 'Attendance',
        subtitle: 'Mark attendance for a student, course and date',
        singular: 'Attendance',
        plural: 'attendance records',
        endpoint: '/api/attendance',
        exportUrl: '/api/export/attendance.csv',
        writeRoles: ['ADMIN', 'FACULTY'],
        defaultSort: { key: 'attendanceDate', dir: 'desc' },
        filter: { param: 'studentId', allLabel: 'All students', options: studentOption },
        columns: [
            { label: 'Date', sort: 'attendanceDate', value: r => r.date },
            { label: 'Student', sort: 'student.studentId', value: r => r.studentCode + ' - ' + r.studentName },
            { label: 'Course', sort: 'course.courseCode', value: r => r.courseCode },
            { label: 'Status', sort: 'status', html: r => '<span class="badge ' + (r.status === 'PRESENT' ? 'ok' : 'bad') + '">' +
                (r.status === 'PRESENT' ? 'Present' : 'Absent') + '</span>' }
        ],
        fields: [
            { name: 'studentId', label: 'Student', asNumber: true, options: studentOption },
            { name: 'courseId', label: 'Course', asNumber: true, options: courseOption },
            { name: 'date', label: 'Date', type: 'date' },
            { name: 'status', label: 'Status', choices: [
                { value: 'PRESENT', text: 'Present' }, { value: 'ABSENT', text: 'Absent' }] }
        ],
        afterChange: loadSummary,
        afterShow: async () => {
            const select = document.getElementById('summary-student');
            const previous = select.value;
            const students = await api('GET', '/api/students');
            select.innerHTML = '<option value="">Select student</option>' + students.map(s =>
                '<option value="' + s.id + '"' + (String(s.id) === previous ? ' selected' : '') + '>' +
                esc(s.studentId) + ' - ' + esc(s.name) + '</option>').join('');
            await loadSummary();
        }
    });

    const panel = document.createElement('div');
    panel.className = 'panel';
    panel.innerHTML = '<h2>Attendance percentage by course</h2>' +
        '<p class="muted">Percentage = classes present / classes held. Below 75% is highlighted.</p>' +
        '<div class="toolbar" style="margin-top:12px"><select id="summary-student"></select></div>' +
        '<div id="summary-result"></div>';
    document.getElementById('page-attendance').appendChild(panel);
    document.getElementById('summary-student').addEventListener('change', loadSummary);
})();
