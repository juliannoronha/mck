document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('questionForm');
    const successMessage = document.getElementById('successMessage');

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = new FormData(form);
        
        // Add CSRF token if it's enabled
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

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
            successMessage.innerHTML = 'An error occurred while submitting the form.';
            successMessage.style.display = 'block';
        });
    });

    function validateForm() {
        let isValid = true;
        const startTime = document.getElementsByName('startTime')[0].value;
        const endTime = document.getElementsByName('endTime')[0].value;
        const pouches = document.getElementsByName('pouchesChecked')[0].value;

        // Validate start time
        if (!startTime.match(/^([01]?[0-9]|2[0-3]):[0-5][0-9]$/)) {
            document.getElementById('startTimeError').textContent = 'Please enter a valid time';
            isValid = false;
        } else {
            document.getElementById('startTimeError').textContent = '';
        }

        // Validate end time
        if (!endTime.match(/^([01]?[0-9]|2[0-3]):[0-5][0-9]$/)) {
            document.getElementById('endTimeError').textContent = 'Please enter a valid time';
            isValid = false;
        } else {
            document.getElementById('endTimeError').textContent = '';
        }

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
                document.getElementById('totalSubmissions').textContent = 'Error';
                document.getElementById('avgTimeDuration').textContent = 'Error';
                document.getElementById('avgPouchesPerHour').textContent = 'Error';
                document.getElementById('totalPouchesChecked').textContent = 'Error';
            });
    }

    // Call this function when the page loads
    fetchOverallProductivity();
});