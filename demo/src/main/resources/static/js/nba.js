// Add this at the beginning of your file
document.addEventListener('DOMContentLoaded', function() {
    // Search toggle functionality
    const searchToggle = document.getElementById('searchToggle');
    const searchSection = document.getElementById('searchSection');

    searchToggle.addEventListener('click', function() {
        const isVisible = searchSection.style.display === 'flex';
        searchSection.style.display = isVisible ? 'none' : 'flex';
        searchToggle.classList.toggle('active');
        
        // Save state to localStorage
        localStorage.setItem('searchSectionVisible', !isVisible);
    });

    // Restore previous state
    const wasVisible = localStorage.getItem('searchSectionVisible') === 'true';
    if (wasVisible) {
        searchSection.style.display = 'flex';
        searchToggle.classList.add('active');
    }
});