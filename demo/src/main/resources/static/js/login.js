        // Fade out logout message
        var logoutMessage = document.getElementById('logoutMessage');
        if (logoutMessage) {
            setTimeout(function() {
                logoutMessage.classList.add('fade-out');
                setTimeout(function() {
                    logoutMessage.style.display = 'none';
                }, 600);
            }, 3000);
        }

        // Fade out login container on form submission
        document.querySelector('form').addEventListener('submit', function(e) {
            e.preventDefault();
            document.getElementById('loginContainer').classList.add('fade-out');
            setTimeout(() => {
                this.submit();
            }, 500);
        });