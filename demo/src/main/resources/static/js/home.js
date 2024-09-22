document.addEventListener('DOMContentLoaded', function() {
    const welcomeMessage = document.getElementById('welcomeMessage');
    
    setTimeout(function() {
        welcomeMessage.style.opacity = '0';
        setTimeout(function() {
            welcomeMessage.style.display = 'none';
        }, 1000); // Wait for fade out to complete before hiding
    }, 6000); // Start fading out after 6 seconds (reduced from 9 seconds)
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
        credentials: 'same-origin'
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

function hasRequiredRole() {
    const userRole = document.body.dataset.userRole;
    return ['ROLE_CHECKER', 'ROLE_MODERATOR', 'ROLE_ADMIN'].includes(userRole);
}

function flashButton(button) {
    button.classList.add('flash-red');
    setTimeout(() => {
        button.classList.remove('flash-red');
    }, 1500); // Flash for 1.5 seconds (3 flashes at 0.5s each)
}