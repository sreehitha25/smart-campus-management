const GRADE_COLORS = { 'A+': '#0f7b6c', 'A': '#3aa596', 'B': '#4f86e0', 'C': '#e0a030', 'D': '#e07a30', 'F': '#c0392b' };

function barChart(points, options) {
    if (points.length === 0) {
        return '<p class="muted">No data yet.</p>';
    }
    const max = options.max || Math.max(...points.map(p => p.value), 1);
    return points.map(p => {
        const width = Math.max(p.value / max * 100, 2);
        const low = options.lowBelow !== undefined && p.value < options.lowBelow;
        return '<div class="bar-row"><span class="bar-label">' + esc(p.label) + '</span>' +
            '<div class="bar-track"><div class="bar-fill' + (low ? ' low' : '') + '" style="width:' + width + '%"></div></div>' +
            '<span class="bar-value">' + options.format(p.value) + '</span></div>';
    }).join('');
}

function donutChart(points) {
    const total = points.reduce((sum, p) => sum + p.value, 0);
    if (total === 0) {
        return '<p class="muted">No marks entered yet.</p>';
    }
    let used = 0;
    const rings = points.filter(p => p.value > 0).map(p => {
        const share = p.value / total * 100;
        const ring = '<circle cx="21" cy="21" r="15.9155" fill="none" stroke="' + GRADE_COLORS[p.label] +
            '" stroke-width="6" stroke-dasharray="' + share + ' ' + (100 - share) +
            '" stroke-dashoffset="' + (25 - used) + '"></circle>';
        used += share;
        return ring;
    }).join('');
    const legend = points.map(p =>
        '<li><span class="dot" style="background:' + GRADE_COLORS[p.label] + '"></span>' +
        esc(p.label) + ' &middot; ' + p.value + '</li>').join('');
    return '<div class="donut-wrap"><svg class="donut" viewBox="0 0 42 42">' +
        '<circle cx="21" cy="21" r="15.9155" fill="none" stroke="#e9edf2" stroke-width="6"></circle>' + rings +
        '<text x="21" y="22.5" text-anchor="middle" font-size="7" font-weight="600" fill="#1c2733">' + total + '</text>' +
        '</svg><ul class="legend">' + legend + '</ul></div>';
}

registerPage('dashboard', async () => {
    const data = await api('GET', '/api/dashboard');
    const root = document.getElementById('page-dashboard');
    const lowAttendance = data.averageAttendance < 75;

    function stat(value, label) {
        return '<div class="stat"><span class="stat-value">' + value + '</span><span class="stat-label">' + label + '</span></div>';
    }

    const recentRows = data.recentStudents.length === 0
        ? '<tr><td class="empty" colspan="4">No students yet. Add one from the Students page.</td></tr>'
        : data.recentStudents.map(s =>
            '<tr><td>' + esc(s.studentId) + '</td><td>' + esc(s.name) + '</td><td>' + esc(s.departmentCode) +
            '</td><td>Year ' + esc(s.year) + '</td></tr>').join('');

    const alert = data.lowAttendanceCount > 0
        ? '<div class="alert"><strong>' + data.lowAttendanceCount + '</strong> student' +
          (data.lowAttendanceCount === 1 ? ' is' : 's are') + ' below 75% attendance. ' +
          '<a href="#reports">View shortage report</a></div>'
        : '';

    root.innerHTML =
        '<header class="page-header"><div><h1>Dashboard</h1><p class="muted">Campus overview</p></div></header>' +
        alert +
        '<div class="stats">' +
        stat(data.totalStudents, 'Students') +
        stat(data.totalFaculty, 'Faculty') +
        stat(data.totalCourses, 'Courses') +
        stat(data.totalDepartments, 'Departments') +
        '<div class="stat"><span class="stat-value">' + data.averageAttendance.toFixed(1) + '%</span>' +
        '<span class="stat-label">Average attendance</span>' +
        '<div class="meter' + (lowAttendance ? ' low' : '') + '"><span style="width:' +
        Math.min(data.averageAttendance, 100) + '%"></span></div></div>' +
        '</div>' +
        '<div class="charts">' +
        '<div class="panel"><h2>Students by department</h2>' +
        barChart(data.studentsByDepartment, { format: v => v }) + '</div>' +
        '<div class="panel"><h2>Attendance by course</h2>' +
        barChart(data.attendanceByCourse, { max: 100, lowBelow: 75, format: v => v.toFixed(0) + '%' }) + '</div>' +
        '<div class="panel"><h2>Grade distribution</h2>' + donutChart(data.gradeDistribution) + '</div>' +
        '</div>' +
        '<h2>Recently added students</h2>' +
        '<div class="panel flush"><table><thead><tr><th>Student ID</th><th>Name</th><th>Department</th><th>Year</th></tr></thead>' +
        '<tbody>' + recentRows + '</tbody></table></div>' +
        '<h2>Go to</h2><div class="quick-links">' +
        '<a class="btn primary" href="#students">Students</a>' +
        '<a class="btn" href="#faculty">Faculty</a>' +
        '<a class="btn" href="#courses">Courses</a>' +
        '<a class="btn" href="#attendance">Mark attendance</a>' +
        '<a class="btn" href="#marks">Enter marks</a>' +
        '<a class="btn" href="#reports">Reports</a></div>';
});
