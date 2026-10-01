createCrudPage({
    key: 'courses',
    title: 'Courses',
    subtitle: 'Courses offered by each department',
    singular: 'Course',
    plural: 'courses',
    endpoint: '/api/courses',
    defaultSort: { key: 'courseCode', dir: 'asc' },
    search: { placeholder: 'Search by course code or name' },
    filter: {
        param: 'department',
        allLabel: 'All departments',
        options: { endpoint: '/api/departments', value: d => d.departmentCode, text: d => d.departmentCode }
    },
    columns: [
        { label: 'Code', sort: 'courseCode', value: r => r.courseCode },
        { label: 'Course name', sort: 'courseName', value: r => r.courseName },
        { label: 'Credits', sort: 'credits', value: r => r.credits },
        { label: 'Semester', sort: 'semester', value: r => r.semester },
        { label: 'Department', sort: 'department.departmentCode', value: r => r.departmentCode }
    ],
    fields: [
        { name: 'courseCode', label: 'Course code', placeholder: 'e.g. CS301' },
        { name: 'courseName', label: 'Course name' },
        { name: 'credits', label: 'Credits', type: 'number' },
        { name: 'semester', label: 'Semester', type: 'number' },
        { name: 'departmentId', label: 'Department', asNumber: true,
          options: { endpoint: '/api/departments', text: d => d.departmentCode + ' - ' + d.departmentName } }
    ]
});
