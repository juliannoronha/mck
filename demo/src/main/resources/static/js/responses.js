let isInitialized = false;

document.addEventListener('DOMContentLoaded', function() {
    if (isInitialized) {
        cleanup();
    }
    isInitialized = true;

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
        const fragment = document.createDocumentFragment();
        data.content.forEach(response => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(response.user.username)}</td>
                <td>${escapeHtml(response.pac.startTime)}</td>
                <td>${escapeHtml(response.pac.endTime)}</td>
                <td>${escapeHtml(response.pac.store)}</td>
                <td>${escapeHtml(response.pac.pouchesChecked)}</td>
                <td data-sort="${response.submissionDate}">${formatDate(response.submissionDate)}</td>
                <td><button class="delete-btn" data-id="${response.id}">Delete</button></td>
            `;
            fragment.appendChild(row);
        });
        const tbody = table.querySelector('tbody');
        tbody.innerHTML = '';
        tbody.appendChild(fragment);
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

    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    const debouncedFetchFilteredResults = debounce(fetchFilteredResults, 300);

    nameFilter.addEventListener('input', debouncedFetchFilteredResults);
    storeFilter.addEventListener('change', debouncedFetchFilteredResults);
    monthFilter.addEventListener('change', debouncedFetchFilteredResults);

    function fetchFilteredResults(page = 0) {
        if (!validateFilters()) {
            return;
        }

        const tableContainer = document.getElementById('responsesTable').parentElement;
        tableContainer.classList.add('loading');
        
        const sanitizedName = encodeURIComponent(nameFilter.value.trim());
        const sanitizedStore = encodeURIComponent(storeFilter.value.trim());
        const sanitizedMonth = monthFilter.value ? encodeURIComponent(monthFilter.value.trim()) : '';
        
        const url = new URL(window.location.href);
        url.searchParams.set('nameFilter', sanitizedName);
        url.searchParams.set('store', sanitizedStore);
        url.searchParams.set('month', sanitizedMonth);
        url.searchParams.set('page', page);

        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.text();
            })
            .then(html => {
                if (!html.trim()) {
                    throw new Error('Empty response received');
                }
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
                showMessage(`Failed to fetch results: ${error.message}`, true);
            })
            .finally(() => {
                tableContainer.classList.remove('loading');
            });
    }

    // Make fetchFilteredResults accessible globally
    window.fetchFilteredResults = fetchFilteredResults;

    submitNameFilterBtn.addEventListener('click', function(e) {
        e.preventDefault();
        if (validateFilters()) {
            fetchFilteredResults();
        }
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
                    if (validateFilters()) {
                        fetchFilteredResults(parseInt(e.target.dataset.page));
                    }
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
    // Validate ID
    if (!id || isNaN(id) || id < 1) {
        showMessage('Invalid response ID', true);
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    if (!csrfToken || !csrfHeader) {
        console.error('CSRF tokens not found');
        return;
    }

    let headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };
    headers[csrfHeader] = csrfToken;

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

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function cleanup() {
    nameFilter.removeEventListener('input', debouncedFetchFilteredResults);
    storeFilter.removeEventListener('change', debouncedFetchFilteredResults);
    monthFilter.removeEventListener('change', debouncedFetchFilteredResults);
}

function validateFilters() {
    const nameFilter = document.getElementById('nameFilter');
    const storeFilter = document.getElementById('storeFilter');
    const monthFilter = document.getElementById('monthFilter');

    // Validate name filter (alphanumeric and spaces only)
    if (nameFilter.value && !/^[a-zA-Z0-9\s]*$/.test(nameFilter.value)) {
        showMessage('Name filter can only contain letters, numbers, and spaces', true);
        return false;
    }

    // Validate store filter (prevent XSS)
    if (storeFilter.value && !/^[A-Za-z0-9\s-]+$/.test(storeFilter.value)) {
        showMessage('Store filter contains invalid characters', true);
        return false;
    }

    // Validate month filter (must be 1-12 or empty)
    if (monthFilter.value && (isNaN(monthFilter.value) || monthFilter.value < 1 || monthFilter.value > 12)) {
        showMessage('Invalid month selected', true);
        return false;
    }

    return true;
}
