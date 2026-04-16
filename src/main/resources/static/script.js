document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const passwordToggles = document.querySelectorAll('[data-toggle-password]');
    const statusMessage = document.getElementById('status-message');
    const navActions = document.getElementById('home-nav-actions');
    const activeRequestsList = document.getElementById('active-requests-list');

    // Password Visibility Toggle
    passwordToggles.forEach(toggle => {
        toggle.addEventListener('click', () => {
            const targetId = toggle.getAttribute('data-toggle-password');
            const input = document.getElementById(targetId);
            if (input) {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                toggle.querySelector('i').classList.toggle('fa-eye');
                toggle.querySelector('i').classList.toggle('fa-eye-slash');
            }
        });
    });

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(loginForm);
            const data = Object.fromEntries(formData.entries());
            const errorElement = document.getElementById('login-error');
            const submitBtn = loginForm.querySelector('button[type="submit"]');

            // Map email to usernameOrEmail for backend compatibility
            data.usernameOrEmail = data.usernameOrEmail; 

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
                
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
                    errorElement.textContent = errorMsg || 'Login failed. Please check your credentials.';
                    errorElement.style.display = 'block';
                    errorElement.className = 'auth-feedback auth-feedback-error';
                }
            } catch (error) {
                console.error('Error:', error);
                errorElement.textContent = 'An error occurred. Please try again later.';
                errorElement.style.display = 'block';
                errorElement.className = 'auth-feedback auth-feedback-error';
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>Login</span> <i class="fas fa-arrow-right"></i>';
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(registerForm);
            const data = Object.fromEntries(formData.entries());
            const messageElement = document.getElementById('register-message');
            const submitBtn = registerForm.querySelector('button[type="submit"]');

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Account...';

                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const msg = await response.text();
                    messageElement.textContent = msg + '. Redirecting to login...';
                    messageElement.className = 'auth-feedback auth-feedback-success';
                    messageElement.style.display = 'block';
                    messageElement.style.background = '#f1fdf4';
                    messageElement.style.color = '#166534';
                    setTimeout(() => window.location.href = 'login.html', 2000);
                } else {
                    const errorMsg = await response.text();
                    messageElement.textContent = errorMsg || 'Registration failed. Check your inputs.';
                    messageElement.className = 'auth-feedback auth-feedback-error';
                    messageElement.style.display = 'block';
                    messageElement.style.background = '#fff1f1';
                    messageElement.style.color = '#c1121f';
                }
            } catch (error) {
                console.error('Error:', error);
                messageElement.textContent = 'An error occurred. Please try again.';
                messageElement.className = 'auth-feedback auth-feedback-error';
                messageElement.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>Register & Save Lives</span> <i class="fas fa-heart"></i>';
            }
        });
    }

    // Auth State Management
    if (statusMessage) {
        const user = JSON.parse(localStorage.getItem('user'));
        if (user) {
            statusMessage.innerHTML = `<strong>Welcome, ${user.username}!</strong>`;
            if (navActions) {
                navActions.innerHTML = `
                    <a href="profile.html" class="btn-hero-secondary" style="padding: 10px 20px;">Profile</a>
                    <button id="logout-btn" class="btn-nav-primary" style="padding: 10px 20px;">Logout</button>
                `;
                document.getElementById('logout-btn').onclick = () => {
                    localStorage.removeItem('user');
                    window.location.reload();
                };
            }
        }
    }

    // Load Statistics and Requests (Simulated or via API)
    if (document.getElementById('total-donors-count')) {
        loadHomeStats();
        loadActiveRequests();
    }

    async function loadHomeStats() {
        try {
            const response = await fetch('/api/home/stats');
            if (response.ok) {
                const stats = await response.json();
                animateCounter('total-donors-count', stats.totalDonors || 0);
                animateCounter('lives-saved-count', stats.livesSaved || 0);
                animateCounter('active-request-count', stats.activeRequests || 0);
            }
        } catch (error) {
            console.error('Stats loading failed:', error);
        }
    }

    async function loadActiveRequests() {
        if (!activeRequestsList) return;
        try {
            const response = await fetch('/api/blood-requests/active');
            if (response.ok) {
                const requests = await response.json();
                if (requests.length === 0) {
                    activeRequestsList.innerHTML = '<p style="text-align: center; color: var(--slate-gray);">No active emergency requests right now.</p>';
                    return;
                }
                activeRequestsList.innerHTML = requests.map(req => `
                    <div class="feature-panel" style="margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
                        <div>
                            <h4 style="margin-bottom: 5px;">${req.bloodGroup} Needed</h4>
                            <p><i class="fas fa-hospital"></i> ${req.hospitalName} | <i class="fas fa-map-marker-alt"></i> ${req.city}</p>
                        </div>
                        <a href="tel:${req.contactNumber}" class="btn-nav-primary">Contact: ${req.contactNumber}</a>
                    </div>
                `).join('');
            }
        } catch (error) {
            console.error('Requests loading failed:', error);
        }
    }

    function animateCounter(id, target) {
        const el = document.getElementById(id);
        let current = 0;
        const duration = 2000;
        const stepTime = Math.abs(Math.floor(duration / target));
        const timer = setInterval(() => {
            current += 1;
            el.textContent = current;
            if (current >= target) {
                el.textContent = target;
                clearInterval(timer);
            }
        }, stepTime || 50);
    }
});
