document.addEventListener('DOMContentLoaded', function() {
    // Form validation
    document.getElementById('questionForm').addEventListener('submit', function(e) {
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

        if (!isValid) {
            e.preventDefault();
        }
    });

    // Success message fade-out
    var successMessage = document.getElementById('successMessage');
    if (successMessage) {
        setTimeout(function() {
            successMessage.classList.add('fade-out');
            setTimeout(function() {
                successMessage.style.display = 'none';
            }, 1000); // Wait for fade out to complete before hiding
        }, 4000); // Start fading out after 4 seconds
    }

    // Fetch overall productivity data
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
            })
            .catch(error => {
                console.error('Error fetching overall productivity:', error);
                document.getElementById('totalSubmissions').textContent = 'Error';
                document.getElementById('avgTimeDuration').textContent = 'Error';
                document.getElementById('avgPouchesPerHour').textContent = 'Error';
            });
    }

    // Call this function when the page loads
    fetchOverallProductivity();
});