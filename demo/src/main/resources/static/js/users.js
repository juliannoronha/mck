var currentUsername = '';
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Function to show delete confirmation modal
function showDeleteConfirmation(username) {
    currentUsername = username;
    document.getElementById('deleteModal').style.display = 'block';
}

// Event listener for confirm delete button
document.getElementById('confirmDelete').addEventListener('click', async function() {
    try {
        const response = await fetch('/users/delete?username=' + encodeURIComponent(currentUsername), {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            }
        });
        
        if (!response.ok) throw new Error('Delete operation failed');
        
        location.reload();
    } catch (error) {
        console.error('Error:', error);
        alert('Error deleting user: ' + error.message);
    } finally {
        document.getElementById('deleteModal').style.display = 'none';
    }
});

// Event listener for cancel delete button
document.getElementById('cancelDelete').addEventListener('click', function() {
    document.getElementById('deleteModal').style.display = 'none';
});

// Close modal when clicking outside of it
window.onclick = function(event) {
    var modal = document.getElementById('deleteModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

// Add this new code for live search functionality
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchInput');
    const usersTable = document.getElementById('usersTable');
    const rows = usersTable.getElementsByTagName('tr');

    const searchHandler = function() {
        const searchTerm = searchInput.value.toLowerCase();

        for (let i = 1; i < rows.length; i++) {
            const username = rows[i].getElementsByTagName('td')[0].textContent.toLowerCase();
            if (username.includes(searchTerm)) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
    };
    
    searchInput.addEventListener('keyup', searchHandler);
    
    // Cleanup function
    window.addEventListener('unload', function() {
        searchInput.removeEventListener('keyup', searchHandler);
    });
});

function deleteUser(username) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/users/delete?username=' + encodeURIComponent(username), {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(data => {
        alert(data);
        location.reload();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error deleting user: ' + error.message);
    });
}

/*]]>*/