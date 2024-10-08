var currentUsername = '';
var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Function to show delete confirmation modal
function showDeleteConfirmation(username) {
    currentUsername = username;
    document.getElementById('deleteModal').style.display = 'block';
}

// Event listener for confirm delete button
document.getElementById('confirmDelete').addEventListener('click', function() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', /*[[@{/users/delete}]]*/ '/users/delete', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.setRequestHeader(csrfHeader, csrfToken);
    xhr.onload = function() {
        if (xhr.status === 200) {
            location.reload(); // Reload the page to reflect changes
        } else {
            // Handle error
            alert(xhr.responseText || 'Error deleting user');
        }
        document.getElementById('deleteModal').style.display = 'none';
    };
    xhr.send('username=' + encodeURIComponent(currentUsername));
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

    searchInput.addEventListener('keyup', function() {
        const searchTerm = searchInput.value.toLowerCase();

        for (let i = 1; i < rows.length; i++) {
            const username = rows[i].getElementsByTagName('td')[0].textContent.toLowerCase();
            if (username.includes(searchTerm)) {
                rows[i].style.display = '';
            } else {
                rows[i].style.display = 'none';
            }
        }
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