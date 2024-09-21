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