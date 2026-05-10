document.addEventListener('DOMContentLoaded', () => {
    const BASE_URL = window.location.port === '5500' ? 'http://localhost:8080' : '';
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const bankRegisterForm = document.getElementById('bank-register-form');
    const passwordToggles = document.querySelectorAll('[data-toggle-password]');
    const statusMessage = document.getElementById('status-message');
    const navActions = document.getElementById('home-nav-actions');
    const activeRequestsList = document.getElementById('active-requests-list');
    const donorSearchForm = document.getElementById('donor-search-form');
    const searchResults = document.getElementById('search-results');

    // Password Visibility Toggle
    passwordToggles.forEach(toggle => {
        toggle.addEventListener('click', () => {
            const targetId = toggle.getAttribute('data-toggle-password');
            const input = document.getElementById(targetId);
            if (input) {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                const icon = toggle.querySelector('i');
                if (icon) {
                    icon.classList.toggle('fa-eye');
                    icon.classList.toggle('fa-eye-slash');
                }
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

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
                
                // Try normal user login first, if it fails with 401, try blood bank login
                let response = await fetch(`${BASE_URL}/api/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const result = await response.json();
                    localStorage.setItem('user', JSON.stringify(result));
                    
                    const urlParams = new URLSearchParams(window.location.search);
                    const redirect = urlParams.get('redirect');
                    window.location.href = redirect ? decodeURIComponent(redirect) : 'dashboard.html';
                } else if (response.status === 401) {
                    // Try Blood Bank Login
                    const bankResponse = await fetch(`${BASE_URL}/api/bloodbank/login`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(data)
                    });

                    if (bankResponse.ok) {
                        const result = await bankResponse.json();
                        localStorage.setItem('user', JSON.stringify(result));
                        
                        const urlParams = new URLSearchParams(window.location.search);
                        const redirect = urlParams.get('redirect');
                        window.location.href = redirect ? decodeURIComponent(redirect) : 'bank-dashboard.html';
                    } else {
                        const errorMsg = await bankResponse.text();
                        errorElement.textContent = errorMsg || 'Login failed. Please check your credentials.';
                        errorElement.style.display = 'block';
                        errorElement.className = 'auth-feedback auth-feedback-error';
                    }
                } else {
                    const errorMsg = await response.text();
                    errorElement.textContent = errorMsg || 'Login failed.';
                    errorElement.style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                errorElement.textContent = 'An error occurred. Please try again later.';
                errorElement.style.display = 'block';
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
            
            // Get userType from the radio toggle in HTML
            const regType = document.querySelector('input[name="regType"]:checked').value;
            data.userType = regType;

            const messageElement = document.getElementById('register-message');
            const submitBtn = registerForm.querySelector('button[type="submit"]');

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Account...';

                const response = await fetch(`${BASE_URL}/api/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const msg = await response.text();
                    messageElement.textContent = msg + '. Redirecting to login...';
                    messageElement.className = 'auth-feedback auth-feedback-success';
                    messageElement.style.display = 'block';
                    setTimeout(() => window.location.href = 'login.html', 2000);
                } else {
                    const errorMsg = await response.text();
                    messageElement.textContent = errorMsg || 'Registration failed.';
                    messageElement.className = 'auth-feedback auth-feedback-error';
                    messageElement.style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                messageElement.textContent = 'An error occurred. Please try again.';
                messageElement.className = 'auth-feedback auth-feedback-error';
                messageElement.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                const label = document.getElementById('user-type-label').textContent;
                submitBtn.innerHTML = `<span>Register as ${label}</span> <i class="fas fa-heart"></i>`;
            }
        });
    }

    if (bankRegisterForm) {
        bankRegisterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(bankRegisterForm);
            const data = Object.fromEntries(formData.entries());
            const messageElement = document.getElementById('register-message');
            const submitBtn = bankRegisterForm.querySelector('button[type="submit"]');

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Registering Blood Bank...';

                const response = await fetch(`${BASE_URL}/api/bloodbank/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                const result = await response.json();
                if (result.status) {
                    messageElement.textContent = result.message + '. Redirecting to login...';
                    messageElement.className = 'auth-feedback auth-feedback-success';
                    messageElement.style.display = 'block';
                    setTimeout(() => window.location.href = 'login.html', 2000);
                } else {
                    messageElement.textContent = result.message || 'Registration failed.';
                    messageElement.className = 'auth-feedback auth-feedback-error';
                    messageElement.style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                messageElement.textContent = 'An error occurred. Please try again.';
                messageElement.className = 'auth-feedback auth-feedback-error';
                messageElement.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>Register Blood Bank</span> <i class="fas fa-shield-halved"></i>';
            }
        });
    }

    // Donor & Blood Bank Search Tab Logic
    const tabDonors = document.getElementById('tab-donors');
    const tabBanks = document.getElementById('tab-banks');
    const searchType = document.getElementById('search-type');
    const searchLabel1 = document.getElementById('search-label-1');
    const bgContainer = document.getElementById('blood-group-container');

    if (tabDonors && tabBanks) {
        tabDonors.onclick = () => {
            searchType.value = 'donors';
            tabDonors.style.background = 'var(--primary-red)';
            tabDonors.style.color = 'white';
            tabBanks.style.background = 'transparent';
            tabBanks.style.color = 'var(--primary-red)';
            searchLabel1.textContent = 'Blood Group';
            bgContainer.style.display = 'block';
            if (searchResults) searchResults.innerHTML = '';
        };

        tabBanks.onclick = () => {
            searchType.value = 'banks';
            tabBanks.style.background = 'var(--primary-red)';
            tabBanks.style.color = 'white';
            tabDonors.style.background = 'transparent';
            tabDonors.style.color = 'var(--primary-red)';
            searchLabel1.textContent = 'Search by Name (Optional)';
            // We'll keep the blood group for banks too, as it can search stock
            // searchLabel1.textContent = 'Blood Bank Name';
            // bgContainer.style.display = 'none'; 
            if (searchResults) searchResults.innerHTML = '';
        };

        // Nav Link Listeners for smooth tab switching
        const navDonorLink = document.getElementById('nav-donor-link');
        const navBankLink = document.getElementById('nav-bank-link');

        if (navDonorLink) {
            navDonorLink.onclick = () => {
                tabDonors.onclick();
            };
        }
        if (navBankLink) {
            navBankLink.onclick = () => tabBanks.onclick();
        }

        const navRequestLink = document.querySelector('a[href="#request-blood"]');
        // No specific listener needed for jump-links unless adding special behavior
    }

    // Donor Search Logic
    if (donorSearchForm) {
        donorSearchForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const bloodGroup = document.getElementById('search-blood-group').value;
            const city = document.getElementById('search-city').value;
            const type = searchType ? searchType.value : 'donors';
            
            if (type === 'donors') {
                performDonorSearch(bloodGroup, city);
            } else {
                performBankSearch(bloodGroup, city);
            }
        });
        // performDonorSearch('', ''); // Removed auto-load to keep home page clean
    }

    async function performDonorSearch(bloodGroup, city) {
        if (!searchResults) return;
        try {
            searchResults.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px;"><i class="fas fa-circle-notch fa-spin fa-2x" style="color: var(--primary-red);"></i><p style="margin-top: 15px;">Finding life-savers...</p></div>';
            
            const params = new URLSearchParams();
            if (bloodGroup) params.append('bloodGroup', bloodGroup);
            if (city) params.append('city', city);
            const donorResponse = await fetch(`${BASE_URL}/api/donors/search?${params.toString()}`);
            
            let donors = [];
            if (donorResponse.ok) {
                donors = await donorResponse.json();
            }

            if (donors.length === 0) {
                searchResults.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; background: var(--slate-light); border-radius: 20px;"><i class="fas fa-search fa-2x" style="color: var(--slate-gray); margin-bottom: 15px;"></i><p>No donors found matching your criteria.</p></div>';
                return;
            }

            searchResults.innerHTML = donors.map(donor => {
                const statusColor = donor.availableToDonate ? '#166534' : '#991b1b';
                const statusBg = donor.availableToDonate ? '#f0fdf4' : '#fef2f2';
                const statusBorder = donor.availableToDonate ? '#dcfce7' : '#fee2e2';
                const statusText = donor.availableToDonate ? 'DONOR AVAILABLE' : 'BUSY / NOT ELIGIBLE';
                const iconColor = donor.availableToDonate ? '#22c55e' : '#ef4444';
                const iconBg = donor.availableToDonate ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)';

                const user = JSON.parse(localStorage.getItem('user'));
                const donorUsername = donor.username || (donor.fullName ? donor.fullName.toLowerCase().replace(/\s+/g, '') : '');
                const detailUrl = `donor-details.html?username=${encodeURIComponent(donorUsername)}`;
                const actionUrl = user ? detailUrl : `login.html?redirect=${encodeURIComponent(detailUrl)}`;

                return `
                    <article class="feature-panel" style="text-align: center; cursor: pointer;" onclick="window.location.href='${actionUrl}'">
                        <div class="feature-panel-icon" style="margin: 0 auto 20px; background: ${iconBg}; color: ${iconColor};">
                            <i class="fas ${donor.availableToDonate ? 'fa-user-check' : 'fa-user-clock'}"></i>
                        </div>
                        <span class="blood-badge" style="background: rgba(230, 57, 70, 0.1); color: var(--primary-red); padding: 4px 12px; border-radius: 100px; font-weight: 700; font-size: 0.8rem;">${donor.bloodGroup}</span>
                        <h3 style="margin: 15px 0 5px; font-size: 1.2rem;">${donor.fullName || 'Hero Donor'}</h3>
                        <p style="font-size: 0.9rem; color: var(--slate-gray); margin-bottom: 15px;"><i class="fas fa-map-marker-alt"></i> ${donor.city || 'Location Hidden'}</p>
                        <div style="padding: 10px; background: ${statusBg}; border-radius: 12px; border: 1px solid ${statusBorder};">
                            <span style="font-size: 0.8rem; font-weight: 600; color: ${statusColor};">${statusText}</span>
                        </div>
                        <button class="btn-nav-primary" style="margin-top: 20px; width: 100%; padding: 10px; font-size: 0.85rem;">View Donor</button>
                    </article>
                `;
            }).join('');
        } catch (error) { console.error(error); }
    }

    async function performBankSearch(bloodGroup, city) {
        if (!searchResults) return;
        try {
            searchResults.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px;"><i class="fas fa-circle-notch fa-spin fa-2x" style="color: var(--primary-red);"></i><p style="margin-top: 15px;">Searching blood bank resources...</p></div>';
            
            const user = JSON.parse(localStorage.getItem('user'));
            const loginRedirect = 'login.html?redirect=index.html#search';

            if (!bloodGroup || bloodGroup === "") {
                // Case: All Groups selected - Show Blood Banks directly
                const params = new URLSearchParams();
                if (city) params.append('city', city);
                const response = await fetch(`${BASE_URL}/api/bloodbank/all?${params.toString()}`);
                
                if (response.ok) {
                    const banks = await response.json();
                    if (banks.length === 0) {
                        searchResults.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; background: var(--slate-light); border-radius: 20px;"><i class="fas fa-hospital fa-2x" style="color: var(--slate-gray); margin-bottom: 15px;"></i><p>No blood banks found in this area.</p></div>';
                        return;
                    }
                    searchResults.innerHTML = banks.map(bank => {
                        const detailUrl = `blood-banks.html?id=${encodeURIComponent(bank.id)}`;
                        const actionUrl = user ? detailUrl : `login.html?redirect=${encodeURIComponent(detailUrl)}`;
                        return `
                            <article class="feature-panel" style="text-align: center; border: 1px solid var(--primary-red); cursor: pointer;" onclick="window.location.href='${actionUrl}'">
                                <div class="feature-panel-icon" style="margin: 0 auto 20px; background: rgba(230, 57, 70, 0.1); color: var(--primary-red);">
                                    <i class="fas fa-hospital"></i>
                                </div>
                                <h3 style="margin: 15px 0 5px; font-size: 1.2rem;">${bank.bankName}</h3>
                                <p style="font-size: 0.9rem; color: var(--slate-gray); margin-bottom: 15px;"><i class="fas fa-map-marker-alt"></i> ${bank.city}</p>
                                <div style="padding: 10px; background: #fef2f2; border-radius: 12px; border: 1px solid #fee2e2;">
                                    <span style="font-size: 0.8rem; font-weight: 600; color: var(--primary-red);">REGISTERED BLOOD BANK</span>
                                </div>
                                <button class="btn-nav-primary" style="margin-top: 20px; width: 100%; padding: 10px; font-size: 0.85rem;">View Bank Details</button>
                            </article>
                        `;
                    }).join('');
                }
            } else {
                // Case: Specific Blood Group selected - Show banks with that stock
                const params = new URLSearchParams();
                params.append('bloodGroup', bloodGroup);
                if (city) params.append('city', city);
                
                const response = await fetch(`${BASE_URL}/api/blood-stock/search?${params.toString()}`);
                
                let stockResults = [];
                if (response.ok) {
                    stockResults = await response.json();
                }

                if (stockResults.length === 0) {
                    searchResults.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; background: var(--slate-light); border-radius: 20px;"><i class="fas fa-hospital fa-2x" style="color: var(--slate-gray); margin-bottom: 15px;"></i><p>No blood banks currently have this group in stock in your area.</p></div>';
                    return;
                }

                searchResults.innerHTML = stockResults.map(stock => {
                    const detailUrl = `blood-banks.html?id=${encodeURIComponent(stock.bloodBankId)}`;
                    const actionUrl = user ? detailUrl : `login.html?redirect=${encodeURIComponent(detailUrl)}`;

                    return `
                        <article class="feature-panel" style="text-align: center; border: 1px solid var(--primary-red); cursor: pointer;" onclick="window.location.href='${actionUrl}'">
                            <div class="feature-panel-icon" style="margin: 0 auto 20px; background: rgba(230, 57, 70, 0.1); color: var(--primary-red);">
                                <i class="fas fa-hospital"></i>
                            </div>
                            <span class="blood-badge" style="background: var(--primary-red); color: white; padding: 4px 12px; border-radius: 100px; font-weight: 700; font-size: 0.8rem;">${stock.bloodGroup}</span>
                            <h3 style="margin: 15px 0 5px; font-size: 1.2rem;">${stock.bloodBankName}</h3>
                            <p style="font-size: 0.9rem; color: var(--slate-gray); margin-bottom: 15px;"><i class="fas fa-warehouse"></i> In Stock: ${stock.units} Units</p>
                            <div style="padding: 10px; background: #fef2f2; border-radius: 12px; border: 1px solid #fee2e2;">
                                <span style="font-size: 0.8rem; font-weight: 600; color: var(--primary-red);">BLOOD BANK STOCK</span>
                            </div>
                            <button class="btn-nav-primary" style="margin-top: 20px; width: 100%; padding: 10px; font-size: 0.85rem;">View Bank Details</button>
                        </article>
                    `;
                }).join('');
            }
        } catch (error) { console.error(error); }
    }

    // Auth State
    if (statusMessage) {
        const user = JSON.parse(localStorage.getItem('user'));
        if (user) {
            const displayName = user.username || user.name || user.bloodBankName || user.bankName || 'User';
            statusMessage.innerHTML = `<strong>Welcome, ${displayName}!</strong>`;
            if (navActions) {
                const userType = String(user.userType || user.type || user.role || '').toLowerCase();
                const isBloodBank = Boolean(
                    userType.includes('bank') ||
                    user.bloodBankId ||
                    user.bloodBankName ||
                    user.bankId ||
                    user.bankName ||
                    user.isBloodBank === true
                );
                const dashboardHref = isBloodBank ? 'bank-dashboard.html' : 'dashboard.html';
                navActions.innerHTML = `
                    <a href="${dashboardHref}" class="btn-hero-secondary" style="padding: 10px 20px;">Dashboard</a>
                    <button id="logout-btn" class="btn-nav-primary" style="padding: 10px 20px;">Logout</button>
                `;
                document.getElementById('logout-btn').onclick = () => {
                    localStorage.removeItem('user');
                    window.location.reload();
                };
            }
        }
    }

    // Stats
    if (document.getElementById('total-donors-count')) {
        loadHomeStats();
        loadActiveRequests();
    }

    async function loadHomeStats() {
        try {
            const response = await fetch(`${BASE_URL}/api/home/stats`);
            if (response.ok) {
                const stats = await response.json();
                
                // Original Stats section
                animateCounter('total-donors-count', stats.totalDonors || 0);
                animateCounter('lives-saved-count', stats.livesSaved || 0);
                animateCounter('active-request-count', stats.activeRequests || 0);

                // Impact section
                animateCounter('impact-donors', stats.totalDonors || 0);
                animateCounter('impact-banks', stats.totalBloodBanks || 0);
                animateCounter('impact-requests', stats.activeRequests || 0);
                
                if (stats.livesSaved > 0) {
                    const livesBox = document.getElementById('impact-lives-box');
                    if (livesBox) {
                        livesBox.style.display = 'flex';
                        animateCounter('impact-lives', stats.livesSaved);
                    }
                }
            }
        } catch (error) { console.error(error); }
    }

    async function loadActiveRequests() {
        if (!activeRequestsList) return;
        try {
            const response = await fetch(`${BASE_URL}/api/blood-requests/active`);
            if (response.ok) {
                const requests = await response.json();
                if (requests.length === 0) {
                    activeRequestsList.innerHTML = '<p style="text-align: center; color: var(--slate-gray);">No active emergency requests.</p>';
                    return;
                }
                activeRequestsList.innerHTML = requests.map(req => {
                    const maskedPhone = req.contactNumber ? req.contactNumber.replace(/.(?=.{4})/g, '*') : 'N/A';
                    const detailUrl = `request-details.html?id=${req.id}`;
                    const loginRedirect = `login.html?redirect=${encodeURIComponent(detailUrl)}`;
                    
                    return `
                        <div class="feature-panel" style="margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
                            <div>
                                <h4 style="margin-bottom: 5px;">Patient Name: ${req.patientName || 'N/A'}</h4>
                                <p style="font-weight: 600; color: var(--primary-red); margin-bottom: 5px;">Blood Group: ${req.bloodGroup}</p>
                                <p><i class="fas fa-hospital"></i> Hospital: ${req.hospitalName} | <i class="fas fa-map-marker-alt"></i> ${req.city}</p>
                            </div>
                            <div style="text-align: right;">
                                <p style="font-size: 0.85rem; color: var(--slate-gray); margin-bottom: 5px;">Contact: ${maskedPhone}</p>
                                <a href="${loginRedirect}" class="btn-nav-primary" style="padding: 8px 15px; font-size: 0.8rem;">View Full Details</a>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        } catch (error) { console.error(error); }
    }

    function animateCounter(id, target) {
        const el = document.getElementById(id);
        if (!el) return;
        let current = 0;
        const duration = 2000;
        const stepTime = Math.abs(Math.floor(duration / target)) || 50;
        const timer = setInterval(() => {
            current += 1;
            el.textContent = current;
            if (current >= target) {
                el.textContent = target;
                clearInterval(timer);
            }
        }, stepTime);
    }
});
