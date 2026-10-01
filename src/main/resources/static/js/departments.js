createCrudPage({
    key: 'departments',
    title: 'Departments',
    subtitle: 'Departments that students, faculty and courses belong to',
    singular: 'Department',
    plural: 'departments',
    endpoint: '/api/departments',
    defaultSort: { key: 'departmentCode', dir: 'asc' },
    columns: [
        { label: 'Code', sort: 'departmentCode', value: r => r.departmentCode },
        { label: 'Name', sort: 'departmentName', value: r => r.departmentName }
    ],
    fields: [
        { name: 'departmentCode', label: 'Department code', placeholder: 'e.g. CSE' },
        { name: 'departmentName', label: 'Department name', placeholder: 'e.g. Computer Science and Engineering' }
    ]
});
