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

        if (!validateForm()) {
            return;
        }

        const formData = new FormData(form);
        
        // Add CSRF token if it's enabled
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const startTimeValue = document.getElementById('startTime').value;
        const endTimeValue = document.getElementById('endTime').value;

        // Use current date for both start and end times
        const currentDate = new Date().toISOString().split('T')[0];
        const startDateTime = `${currentDate}T${startTimeValue}:00`;
        const endDateTime = `${currentDate}T${endTimeValue}:00`;

        // Replace the time inputs with the full date-time strings
        formData.set('startTime', startDateTime);
        formData.set('endTime', endDateTime);

        fetch('/submit-questions', {
            method: 'POST',
            body: formData,
            headers: csrfHeader ? {
                [csrfHeader]: csrfToken
            } : {},
            credentials: 'same-origin'
        })
        .then(response => {
            console.log('Response status:', response.status);
            return response.text();
        })
        .then(data => {
            console.log('Response data:', data);
            successMessage.innerHTML = data;
            successMessage.style.display = 'block';
            form.reset();

            // Hide the success message after 3 seconds
            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 3000);

            // Refresh the dashboard data
            fetchOverallProductivity();
        })
        .catch(error => {
            console.error('Error:', error);
            let errorMessage = 'An error occurred while submitting the form.';
            if (error instanceof TypeError && error.message.includes('NetworkError')) {
                errorMessage += ' This might be due to a secure connection issue. Please ensure you\'re using HTTPS.';
            }
            successMessage.innerHTML = errorMessage;
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

    // Call this function when the page loads
    fetchOverallProductivity();
});