document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('questionForm');
    const successMessage = document.getElementById('successMessage');
    const startTime = document.getElementById('startTime');
    const endTime = document.getElementById('endTime');

    // New time input validation functions
    function validateTimeInput(input) {
        const timeRegex = /^([01]\d|2[0-3]):([0-5]\d)$/;
        return timeRegex.test(input.value);
    }

    function updateEndTimeMin() {
        if (startTime.value) {
            endTime.min = startTime.value;
        }
    }

    startTime.addEventListener('change', function() {
        if (!validateTimeInput(this)) {
            document.getElementById('startTimeError').textContent = 'Please enter a valid time';
        } else {
            document.getElementById('startTimeError').textContent = '';
            updateEndTimeMin();
        }
    });

    endTime.addEventListener('change', function() {
        if (!validateTimeInput(this)) {
            document.getElementById('endTimeError').textContent = 'Please enter a valid time';
        } else if (this.value <= startTime.value) {
            document.getElementById('endTimeError').textContent = 'End time must be after start time';
        } else {
            document.getElementById('endTimeError').textContent = '';
        }
    });

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Clear previous error messages
        document.querySelectorAll('.error').forEach(el => el.textContent = '');

        // Validate form inputs
        let isValid = true;
        if (!validateTimeInput(startTime)) {
            document.getElementById('startTimeError').textContent = 'Please enter a valid time';
            isValid = false;
        }
        if (!validateTimeInput(endTime)) {
            document.getElementById('endTimeError').textContent = 'Please enter a valid time';
            isValid = false;
        } else if (endTime.value <= startTime.value) {
            document.getElementById('endTimeError').textContent = 'End time must be after start time';
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        const formData = new FormData(form);
        const currentDate = new Date().toISOString().split('T')[0]; // Get current date in YYYY-MM-DD format
        const pacData = {
            store: formData.get('store'),
            startTime: `${currentDate}T${formData.get('startTime')}:00`,
            endTime: `${currentDate}T${formData.get('endTime')}:00`,
            pouchesChecked: parseInt(formData.get('pouchesChecked'))
        };

        console.log('Submitting data:', pacData); // Log the data being sent

        fetch('/submit-questions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(pacData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(data => {
            successMessage.textContent = data;
            successMessage.style.display = 'block';
            form.reset();
            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 5000);
        })
        .catch(error => {
            console.error('Error:', error);
            showErrorMessage('An error occurred. Please try again later.');
        });
    });

    function showErrorMessage(message) {
        const errorDiv = document.getElementById('errorMessage');
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    function validateForm() {
        let isValid = true;
        const pouches = document.getElementsByName('pouchesChecked')[0].value;

        // Time validation is now handled by the change event listeners

        // Validate pouches checked
        if (isNaN(pouches) || pouches < 0) {
            document.getElementById('pouchesError').textContent = 'Please enter a valid number';
            isValid = false;
        } else {
            document.getElementById('pouchesError').textContent = '';
        }

        return isValid;
    }

    function fetchOverallProductivity() {
        console.log('Fetching productivity data...');
        fetch('/api/overall-productivity')
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Data received:', JSON.stringify(data));
                updateDashboard(data);
                if (data.chartData) {
                    createPacMedChart(data.chartData);
                }
            })
            .catch(error => {
                console.error('Error fetching overall productivity:', error);
            });
    }

    function updateDashboard(data) {
        document.getElementById('totalSubmissions').textContent = data.totalSubmissions ?? 'N/A';
        document.getElementById('avgTimePerPouch').textContent = 
            data.avgTimePerPouch != null ? formatDuration(data.avgTimePerPouch) : 'N/A';
        document.getElementById('avgPouchesPerHour').textContent = 
            data.avgPouchesPerHour != null ? data.avgPouchesPerHour.toFixed(2) : 'N/A';
        document.getElementById('totalPouchesChecked').textContent = data.totalPouchesChecked ?? 'N/A';
    }

    function formatDuration(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = Math.round(seconds % 60);
        return `${minutes}m ${remainingSeconds.toString().padStart(2, '0')}s`;
    }

    function setupSSEConnection() {
        const eventSource = new EventSource('/api/overall-productivity-stream');
        eventSource.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                if (Array.isArray(data)) {
                    // Handle individual user productivity updates
                    // You can implement this if needed
                } else {
                    // Handle overall productivity update
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

    setupSSEConnection();

    function validateInput(input) {
        const errorId = `${input.id}Error`;
        const errorElement = document.getElementById(errorId);
        
        // If error element doesn't exist, create it
        if (!errorElement && input.parentElement) {
            const newErrorElement = document.createElement('div');
            newErrorElement.id = errorId;
            newErrorElement.className = 'error';
            input.parentElement.appendChild(newErrorElement);
        }

        const currentErrorElement = document.getElementById(errorId);
        if (!currentErrorElement) return; // Safety check

        if (input.validity.valid) {
            currentErrorElement.textContent = '';
            input.classList.remove('invalid');
        } else {
            let errorMessage = '';
            
            switch(input.id) {
                case 'startTime':
                case 'endTime':
                    errorMessage = 'Please enter a valid time';
                    break;
                case 'store':
                    errorMessage = 'Please select a store';
                    break;
                case 'pouchesChecked':
                    errorMessage = 'Please enter a valid number';
                    break;
                default:
                    errorMessage = input.validationMessage;
            }
            
            currentErrorElement.textContent = errorMessage;
            input.classList.add('invalid');
        }
    }

    function addRealTimeValidation() {
        const form = document.getElementById('questionForm');
        if (!form) return;

        const inputs = form.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                validateInput(this);
            });
        });
    }

    addRealTimeValidation();

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

fetchOverallProductivity();
});