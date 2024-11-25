document.addEventListener('DOMContentLoaded', function() {
    const welcomeMessage = document.getElementById('welcomeMessage');
    
    setTimeout(function() {
        welcomeMessage.style.opacity = '0';
        setTimeout(function() {
            welcomeMessage.style.display = 'none';
        }, 1000); // Wait for fade out to complete before hiding
    }, 6000); // Start fading out after 6 seconds (reduced from 9 seconds)

    // Fetch dashboard data if user is ADMIN or MODERATOR
    if (hasRequiredRole()) {
        fetchOverallProductivity();
        setupSSEConnection();
    }
});

function fadeOutAndNavigate(url, event) {
    event.preventDefault();
    fadeOutElements();
    setTimeout(() => {
        window.location.href = url;
    }, 500);
}

function fadeOutAndSubmit(form, event) {
    event.preventDefault();
    fadeOutElements();
    setTimeout(() => {
        form.submit();
    }, 500);
}

function fadeOutElements() {
    document.getElementById('mainNav').classList.add('fade-out');
    document.getElementById('mainContent').classList.add('fade-out');
}

function showAccessDeniedMessage() {
    const message = document.getElementById('accessDeniedMessage');
    message.classList.remove('hide');
    message.classList.add('show');
    message.style.display = 'block';
    
    setTimeout(() => {
        message.classList.remove('show');
        message.classList.add('hide');
        setTimeout(() => {
            message.style.display = 'none';
            message.classList.remove('hide');
        }, 500); // Match the duration of the fadeOutDown animation
    }, 3000); // Show for 3 seconds
}

function handleUnauthorizedAccess(url) {
    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
    }).then(response => {
        if (response.status === 403) {
            showAccessDeniedMessage();
        } else {
            window.location.href = url;
        }
    }).catch(error => {
        console.error('Error:', error);
    });
}

function handlePacMedClick(event) {
    event.preventDefault();
    const pacmedButton = document.getElementById('pacmedButton');
    
    if (hasRequiredRole()) {
        window.location.href = '/packmed';
    } else {
        showAccessDeniedMessage();
        flashButton(pacmedButton);
    }
}

function handleWellcaClick(event) {
    event.preventDefault();
    const wellcaButton = document.getElementById('wellcaButton');
    
    if (hasRequiredRole()) {
        window.location.href = '/wellca';
    } else {
        showAccessDeniedMessage();
        flashButton(wellcaButton);
    }
}

function handleNBAClick(event) {
    event.preventDefault();
    const nbaButton = document.getElementById('nbaButton');
    
    if (hasNBAAccess()) {
        window.location.href = '/nbaplayers';
    } else {
        showAccessDeniedMessage();
        flashButton(nbaButton);
    }
}

function hasRequiredRole() {
    const userRole = document.body.dataset.userRole;
    return ['ROLE_MODERATOR', 'ROLE_ADMIN', 'ROLE_CHECKER', 'ROLE_SHIPPING', 'ROLE_INVENTORY'].includes(userRole);
}

function hasNBAAccess() {
    const userRole = document.body.dataset.userRole;
    return ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR'].includes(userRole);
}

function flashButton(button) {
    button.classList.add('flash-red');
    setTimeout(() => {
        button.classList.remove('flash-red');
    }, 1500); // Flash for 1.5 seconds (3 flashes at 0.5s each)
}

function fetchOverallProductivity() {
    fetch('/api/overall-productivity')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (!data || typeof data !== 'object') {
                throw new Error('Invalid data format received');
            }
            console.log('Received data:', JSON.stringify(data, null, 2));
            updateDashboard(data);
            if (data.chartData && Object.keys(data.chartData).length > 0) {
                console.log('Chart data:', JSON.stringify(data.chartData, null, 2));
                createPacMedChart(data.chartData);
            } else {
                console.error('Chart data is missing or empty');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            updateDashboardError(error.message);
        });
}

function updateDashboard(data) {
    document.getElementById('totalSubmissions').textContent = data.totalSubmissions ?? 'N/A';
    document.getElementById('totalPouchesChecked').textContent = data.totalPouchesChecked ?? 'N/A';
    document.getElementById('avgTimePerPouch').textContent = 
        data.avgTimePerPouch != null ? formatDuration(data.avgTimePerPouch) : 'N/A';
    document.getElementById('avgPouchesPerHour').textContent = 
        data.avgPouchesPerHour != null ? data.avgPouchesPerHour.toFixed(2) : 'N/A';
}

function updateDashboardError(errorMessage) {
    const errorText = 'Error: ' + errorMessage;
    document.getElementById('totalSubmissions').textContent = errorText;
    document.getElementById('totalPouchesChecked').textContent = errorText;
    document.getElementById('avgTimePerPouch').textContent = errorText;
    document.getElementById('avgPouchesPerHour').textContent = errorText;
}

function formatDuration(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.round(seconds % 60);
    return `${minutes}m ${remainingSeconds.toString().padStart(2, '0')}s`;
}

let eventSource;

function setupSSEConnection() {
    if (eventSource) {
        eventSource.close();
    }
    eventSource = new EventSource('/api/overall-productivity-stream');
    window.addEventListener('beforeunload', () => {
        if (eventSource) {
            eventSource.close();
        }
    });
    eventSource.onmessage = function(event) {
        try {
            const data = JSON.parse(event.data);
            if (!Array.isArray(data)) {
                updateDashboard(data);
            }
        } catch (error) {
            console.error('Error parsing SSE data:', error);
        }
    };
    eventSource.onerror = function(error) {
        console.error('Error in SSE connection:', error);
        eventSource.close();
        setTimeout(setupSSEConnection, 5000);
    };
}

let pacMedChart = null;

function createPacMedChart(data) {
    console.log('Creating chart with data:', JSON.stringify(data, null, 2));
    if (!data || !data.labels || !data.pouchesChecked) {
        console.error('Invalid chart data');
        return;
    }
    const ctx = document.getElementById('pacMedChart');
    if (!ctx) {
        console.error('Canvas element not found');
        return;
    }

    // Destroy existing chart if it exists
    if (pacMedChart) {
        pacMedChart.destroy();
    }

    pacMedChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels,
            datasets: [{
                label: 'Pouches Checked',
                data: data.pouchesChecked,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            },
            plugins: {
                title: {
                    display: true,
                    text: 'Pouches Checked (Last 7 Days)',
                    font: {
                        size: 16
                    }
                },
                subtitle: {
                    display: true,
                    text: data.pouchesChecked.every(val => val === 0) ? 'Sample data shown (no actual data available)' : '',
                    color: 'red',
                    font: {
                        size: 14,
                        style: 'italic'
                    }
                }
            }
        }
    });
}

// Call fetchOverallProductivity when the page loads
document.addEventListener('DOMContentLoaded', fetchOverallProductivity);
