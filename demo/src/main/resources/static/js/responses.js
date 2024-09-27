document.addEventListener('DOMContentLoaded', function() {
    if (window.location.protocol !== 'https:') {
        console.warn('This page is not being served over HTTPS. Some features may not work correctly.');
    }

    const table = document.getElementById('responsesTable');
    const nameFilter = document.getElementById('nameFilter');
    const submitNameFilterBtn = document.getElementById('submitNameFilter');
    const storeFilter = document.getElementById('storeFilter');
    const monthFilter = document.getElementById('monthFilter');
    const resetFilterBtn = document.getElementById('resetFilter');

    table.addEventListener('click', function(e) {
        if (e.target.classList.contains('delete-btn')) {
            if (confirm('Are you sure you want to delete this response?')) {
                deleteResponse(e.target.dataset.id);
            }
        }
    });

    function updateTable(data) {
        const tbody = table.querySelector('tbody');
        tbody.innerHTML = '';
        data.content.forEach(response => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${response.user.username}</td>
                <td>${response.pac.startTime}</td>
                <td>${response.pac.endTime}</td>
                <td>${response.pac.store}</td>
                <td>${response.pac.pouchesChecked}</td>
                <td data-sort="${response.submissionDate}">${formatDate(response.submissionDate)}</td>
                <td><button class="delete-btn" data-id="${response.id}">Delete</button></td>
            `;
            tbody.appendChild(row);
        });
        updatePagination(data);
    }

    function updatePagination(data) {
        const paginationContainer = document.querySelector('.pagination-container');
        if (paginationContainer) {
            if (data.totalPages > 1) {
                paginationContainer.innerHTML = createPaginationHTML(data);
                paginationContainer.style.display = 'block';
            } else {
                paginationContainer.style.display = 'none';
            }
        }
    }

    function createPaginationHTML(data) {
        let html = '<ul class="pagination">';
        for (let i = 0; i < data.totalPages; i++) {
            if (i === data.number) {
                html += `<li class="active"><span>${i + 1}</span></li>`;
            } else {
                html += `<li><a href="#" data-page="${i}">${i + 1}</a></li>`;
            }
        }
        html += '</ul>';
        return html;
    }

    function formatDate(dateString) {
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    }

    function fetchFilteredResults(page = 0) {
        const url = new URL(window.location.href);
        url.searchParams.set('nameFilter', nameFilter.value);
        url.searchParams.set('store', storeFilter.value);
        url.searchParams.set('month', monthFilter.value || '');
        url.searchParams.set('page', page);

        fetch(url)
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newTable = doc.getElementById('responsesTable');
                const newPagination = doc.querySelector('.pagination-container');

                if (newTable) {
                    document.getElementById('responsesTable').innerHTML = newTable.innerHTML;
                }
                if (newPagination) {
                    document.querySelector('.pagination-container').innerHTML = newPagination.innerHTML;
                }
                attachPaginationListeners();
            })
            .catch(error => {
                console.error('Error:', error);
                showMessage('Error fetching results: ' + error.message, true);
            });
    }

    // Make fetchFilteredResults accessible globally
    window.fetchFilteredResults = fetchFilteredResults;

    submitNameFilterBtn.addEventListener('click', function() {
        fetchFilteredResults();
    });

    storeFilter.addEventListener('change', function() {
        fetchFilteredResults();
    });

    monthFilter.addEventListener('change', function() {
        fetchFilteredResults();
    });

    resetFilterBtn.addEventListener('click', function() {
        nameFilter.value = '';
        storeFilter.value = '';
        monthFilter.value = '';
        fetchFilteredResults();
    });

    function attachPaginationListeners() {
        const paginationContainer = document.querySelector('.pagination-container');
        if (paginationContainer) {
            paginationContainer.addEventListener('click', function(e) {
                if (e.target.tagName === 'A' && e.target.dataset.page) {
                    e.preventDefault();
                    fetchFilteredResults(parseInt(e.target.dataset.page));
                }
            });
        }
    }

    // Initial load
    attachPaginationListeners();
});

function showMessage(message, isError = false) {
    const messageBubble = document.getElementById('messageBubble');
    const messageText = document.getElementById('messageText');
    messageText.textContent = message;
    messageBubble.className = isError ? 'message-bubble error' : 'message-bubble success';
    messageBubble.style.display = 'block';
    setTimeout(() => {
        messageBubble.style.display = 'none';
    }, 5000);
}

function deleteResponse(id) {
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    if (csrfToken && csrfHeader) {
        headers[csrfHeader.getAttribute('content')] = csrfToken.getAttribute('content');
    }

    fetch('/delete-response', {
        method: 'POST',
        headers: headers,
        body: `id=${id}`
    })
    .then(response => response.text())
    .then(data => {
        showMessage(data);
        window.fetchFilteredResults(); // Use the global function
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('Error deleting response: ' + error.message, true);
    });
}
