document.addEventListener('DOMContentLoaded', function() {
    const nameFilter = document.getElementById('nameFilter');
    const storeFilter = document.getElementById('storeFilter');
    const monthFilter = document.getElementById('monthFilter');
    const resetFilterBtn = document.getElementById('resetFilter');
    const table = document.getElementById('responsesTable');
    const rows = table.getElementsByTagName('tr');

    // Function to apply filters and sort
    function applyFiltersAndSort() {
        const nameValue = nameFilter.value.toLowerCase();
        const storeValue = storeFilter.value;
        const monthValue = monthFilter.value;

        let visibleRows = [];

        for (let i = 1; i < rows.length; i++) {
            const name = rows[i].cells[0].textContent.toLowerCase();
            const store = rows[i].cells[3].textContent.trim();
            const dateStr = rows[i].cells[5].getAttribute('data-sort');
            const date = new Date(dateStr);
            const month = date.getMonth() + 1; // getMonth() returns 0-11

            const nameMatch = name.includes(nameValue);
            const storeMatch = storeValue === '' || store === storeValue;
            const monthMatch = monthValue === '' || month === parseInt(monthValue);

            if (nameMatch && storeMatch && monthMatch) {
                rows[i].style.display = '';
                visibleRows.push(rows[i]);
            } else {
                rows[i].style.display = 'none';
            }
        }

        // Sort visible rows
        visibleRows.sort((a, b) => {
            const dateA = new Date(a.cells[5].getAttribute('data-sort'));
            const dateB = new Date(b.cells[5].getAttribute('data-sort'));
            return dateA - dateB;
        });

        // Reorder the table
        const tbody = table.querySelector('tbody');
        visibleRows.forEach(row => tbody.appendChild(row));
    }

    // Add event listeners for live filtering
    nameFilter.addEventListener('input', applyFiltersAndSort);
    storeFilter.addEventListener('change', applyFiltersAndSort);
    monthFilter.addEventListener('change', applyFiltersAndSort);

    // Reset filter
    resetFilterBtn.addEventListener('click', function() {
        nameFilter.value = '';
        storeFilter.value = '';
        monthFilter.value = '';
        applyFiltersAndSort();
    });

    // Initial sort
    applyFiltersAndSort();
});