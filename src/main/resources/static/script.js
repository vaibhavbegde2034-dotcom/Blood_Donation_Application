document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(loginForm);
            const data = Object.fromEntries(formData.entries());
            const errorElement = document.getElementById('login-error');

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const result = await response.json();
                    localStorage.setItem('user', JSON.stringify(result));
                    window.location.href = 'index.html';
                } else {
                    const errorMsg = await response.text();
                    errorElement.textContent = errorMsg || 'Login failed';
                    errorElement.style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                errorElement.textContent = 'An error occurred. Please try again.';
                errorElement.style.display = 'block';
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(registerForm);
            const data = Object.fromEntries(formData.entries());
            const messageElement = document.getElementById('register-message');
            
            console.log('Attempting to register:', data.username);

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const msg = await response.text();
                    console.log('Registration success:', msg);
                    messageElement.textContent = msg + '. Redirecting to login...';
                    messageElement.className = 'message-text';
                    messageElement.style.display = 'block';
                    setTimeout(() => window.location.href = 'login.html', 2000);
                } else {
                    const errorMsg = await response.text();
                    console.error('Registration failed:', response.status, errorMsg);
                    messageElement.textContent = `Error ${response.status}: ${errorMsg || 'Registration failed. Check inputs or database.'}`;
                    messageElement.className = 'error-text';
                    messageElement.style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                messageElement.textContent = 'An error occurred. Please try again.';
                messageElement.className = 'error-text';
                messageElement.style.display = 'block';
            }
        });
    }

    // Check if logged in on index page
    const statusMessage = document.getElementById('status-message');
    if (statusMessage) {
        const user = JSON.parse(localStorage.getItem('user'));
        if (user) {
            statusMessage.innerHTML = `Hello, ${user.username}! Welcome back. <br><br> <a href="profile.html" class="btn" style="display: inline-block; width: auto; text-decoration: none;">Go to Profile</a>`;
            const logoutBtn = document.createElement('button');
            logoutBtn.textContent = 'Logout';
            logoutBtn.className = 'btn';
            logoutBtn.style.marginTop = '20px';
            logoutBtn.onclick = () => {
                localStorage.removeItem('user');
                window.location.reload();
            };
            statusMessage.parentElement.appendChild(logoutBtn);
        } else {
            statusMessage.innerHTML = 'Please <a href="login.html">Login</a> or <a href="register.html">Register</a> to continue.';
        }
    }
});
