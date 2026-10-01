(function () {
    const studentOption = { endpoint: '/api/students', text: s => s.studentId + ' - ' + s.name };
    const courseOption = { endpoint: '/api/courses', text: c => c.courseCode + ' - ' + c.courseName };

    function gradeBadge(grade) {
        const cls = grade === 'F' ? 'bad' : (grade === 'D' ? 'warn' : 'ok');
        return '<span class="badge ' + cls + '">' + esc(grade) + '</span>';
    }

    createCrudPage({
        key: 'marks',
        title: 'Marks',
        subtitle: 'Internal (out of 40) + external (out of 60). Grades: A+ 90 and above, A 80-89, B 70-79, C 60-69, D 50-59, F below 50.',
        singular: 'Marks',
        plural: 'marks records',
        endpoint: '/api/marks',
        exportUrl: '/api/export/marks.csv',
        writeRoles: ['ADMIN', 'FACULTY'],
        defaultSort: { key: 'student.studentId', dir: 'asc' },
        filter: { param: 'studentId', allLabel: 'All students', options: studentOption },
        columns: [
            { label: 'Student', sort: 'student.studentId', value: r => r.studentCode + ' - ' + r.studentName },
            { label: 'Course', sort: 'course.courseCode', value: r => r.courseCode },
            { label: 'Internal', sort: 'internalMarks', value: r => r.internalMarks },
            { label: 'External', sort: 'externalMarks', value: r => r.externalMarks },
            { label: 'Total', sort: 'totalMarks', value: r => r.totalMarks },
            { label: 'Grade', sort: 'grade', html: r => gradeBadge(r.grade) }
        ],
        fields: [
            { name: 'studentId', label: 'Student', asNumber: true, options: studentOption },
            { name: 'courseId', label: 'Course', asNumber: true, options: courseOption },
            { name: 'internalMarks', label: 'Internal marks (0-40)', type: 'number' },
            { name: 'externalMarks', label: 'External marks (0-60)', type: 'number' }
        ]
    });
})();
