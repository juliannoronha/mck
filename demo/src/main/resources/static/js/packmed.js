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

    // Existing form submission code
    form.addEventListener('submit', function(e) {
        e.preventDefault();

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
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(pacData)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(message => {
            successMessage.textContent = message;
            successMessage.style.display = 'block';
            form.reset();
            setTimeout(() => {
                successMessage.classList.add('fade-out');
            }, 3000);
            setTimeout(() => {
                successMessage.style.display = 'none';
                successMessage.classList.remove('fade-out');
            }, 4000);
        })
        .catch(error => {
            console.error('Error:', error);
            successMessage.textContent = 'An error occurred while submitting the form: ' + error.message;
            successMessage.style.display = 'block';
        });
    });

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

    // Existing fetchOverallProductivity function
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
                document.getElementById('totalSubmissions').textContent = data.totalSubmissions ?? 'N/A';
                document.getElementById('avgTimeDuration').textContent = data.avgTimeDuration ?? 'N/A';
                document.getElementById('avgPouchesPerHour').textContent = 
                    data.avgPouchesPerHour != null ? data.avgPouchesPerHour.toFixed(2) : 'N/A';
                document.getElementById('totalPouchesChecked').textContent = data.totalPouchesChecked ?? 'N/A';
            })
            .catch(error => {
                console.error('Error fetching overall productivity:', error);
                document.getElementById('totalSubmissions').textContent = 'Error: ' + error.message;
                document.getElementById('avgTimeDuration').textContent = 'Error: ' + error.message;
                document.getElementById('avgPouchesPerHour').textContent = 'Error: ' + error.message;
                document.getElementById('totalPouchesChecked').textContent = 'Error: ' + error.message;
            });
    }

    function updateDashboard(data) {
        document.getElementById('totalSubmissions').textContent = data.totalSubmissions ?? 'N/A';
        document.getElementById('avgTimeDuration').textContent = data.avgTimeDuration ?? 'N/A';
        document.getElementById('avgPouchesPerHour').textContent = 
            data.avgPouchesPerHour != null ? data.avgPouchesPerHour.toFixed(2) : 'N/A';
        document.getElementById('totalPouchesChecked').textContent = data.totalPouchesChecked ?? 'N/A';
    }

    function setupSSEConnection() {
        const eventSource = new EventSource('/api/overall-productivity-stream');
        eventSource.onmessage = function(event) {
            console.log('Received SSE event:', event);
            const data = JSON.parse(event.data);
            updateDashboard(data);
        };
        eventSource.onerror = function(error) {
            console.error('Error in SSE connection:', error);
            eventSource.close();
            // Attempt to reconnect after a delay
            setTimeout(setupSSEConnection, 5000);
        };
    }

    // Call this function when the page loads
    setupSSEConnection();
});