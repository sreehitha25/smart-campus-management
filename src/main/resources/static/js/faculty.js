createCrudPage({
    key: 'faculty',
    title: 'Faculty',
    subtitle: 'Manage teaching staff',
    singular: 'Faculty member',
    plural: 'faculty members',
    endpoint: '/api/faculty',
    defaultSort: { key: 'facultyId', dir: 'asc' },
    search: { placeholder: 'Search by name or faculty ID' },
    filter: {
        param: 'department',
        allLabel: 'All departments',
        options: { endpoint: '/api/departments', value: d => d.departmentCode, text: d => d.departmentCode }
    },
    columns: [
        { label: 'Faculty ID', sort: 'facultyId', value: r => r.facultyId },
        { label: 'Name', sort: 'name', value: r => r.name },
        { label: 'Email', sort: 'email', value: r => r.email },
        { label: 'Department', sort: 'department.departmentCode', value: r => r.departmentCode },
        { label: 'Designation', sort: 'designation', value: r => r.designation }
    ],
    fields: [
        { name: 'facultyId', label: 'Faculty ID', placeholder: 'e.g. FAC101' },
        { name: 'name', label: 'Full name' },
        { name: 'email', label: 'Email', type: 'email' },
        { name: 'departmentId', label: 'Department', asNumber: true,
          options: { endpoint: '/api/departments', text: d => d.departmentCode + ' - ' + d.departmentName } },
        { name: 'designation', label: 'Designation', placeholder: 'e.g. Assistant Professor' }
    ]
});
