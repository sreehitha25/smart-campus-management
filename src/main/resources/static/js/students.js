createCrudPage({
    key: 'students',
    title: 'Students',
    subtitle: 'Add, search and update student records',
    singular: 'Student',
    plural: 'students',
    endpoint: '/api/students',
    exportUrl: '/api/export/students.csv',
    defaultSort: { key: 'studentId', dir: 'asc' },
    search: { placeholder: 'Search by name or student ID' },
    filter: {
        param: 'department',
        allLabel: 'All departments',
        options: { endpoint: '/api/departments', value: d => d.departmentCode, text: d => d.departmentCode }
    },
    columns: [
        { label: 'Student ID', sort: 'studentId', value: r => r.studentId },
        { label: 'Name', sort: 'name', value: r => r.name },
        { label: 'Email', sort: 'email', value: r => r.email },
        { label: 'Phone', value: r => r.phone },
        { label: 'Department', sort: 'department.departmentCode', value: r => r.departmentCode },
        { label: 'Year', sort: 'year', value: r => r.year },
        { label: 'Section', sort: 'section', value: r => r.section }
    ],
    fields: [
        { name: 'studentId', label: 'Student ID', placeholder: 'e.g. 22CSE101' },
        { name: 'name', label: 'Full name' },
        { name: 'email', label: 'Email', type: 'email' },
        { name: 'phone', label: 'Phone', placeholder: '10 digits' },
        { name: 'departmentId', label: 'Department', asNumber: true,
          options: { endpoint: '/api/departments', text: d => d.departmentCode + ' - ' + d.departmentName } },
        { name: 'year', label: 'Year', type: 'number' },
        { name: 'section', label: 'Section', placeholder: 'A' },
        { name: 'dateOfBirth', label: 'Date of birth', type: 'date' }
    ]
});
