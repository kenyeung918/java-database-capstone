/*
  Step-by-Step Explanation of Header Section Rendering
  This code dynamically renders the header section of the page based on the user's role, session status, and available actions.
*/

// 1. Define the `renderHeader` Function
function renderHeader() {
    
    // 2. Select the Header Div
    const headerDiv = document.getElementById("header");
    if (!headerDiv) {
        console.error("Header element not found");
        return;
    }

    // 3. Check if the Current Page is the Root Page
    if (window.location.pathname === "/" || window.location.pathname.endsWith("index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
        return;
    }

    // 4. Retrieve the User's Role and Token from LocalStorage
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // 5. Initialize Header Content
    let headerContent = `<header class="header">
        <div class="logo-section">
            <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
            <span class="logo-title">Hospital CMS</span>
        </div>
        <nav class="header-nav">`;

    // 6. Handle Session Expiry or Invalid Login
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("userId");
        localStorage.removeItem("patientId");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    // 7. Add Role-Specific Header Content
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn">Add Doctor</button>
            <a href="#" class="logout-link">Logout</a>`;
    } else if (role === "doctor") {
        headerContent += `
            <button id="homeBtn" class="adminBtn">Home</button>
            <a href="#" class="logout-link">Logout</a>`;
    } else if (role === "patient") {
        headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } else if (role === "loggedPatient") {
        headerContent += `
            <button id="homeBtn" class="adminBtn">Home</button>
            <button id="patientAppointments" class="adminBtn">Appointments</button>
            <a href="#" class="logout-link">Logout</a>`;
    } else {
        // Default header for unauthenticated users
        headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    }

    // 8. Close the Header Section
    headerContent += `</nav></header>`;

    // 9. Render the Header Content
    headerDiv.innerHTML = headerContent;

    // 10. Attach Event Listeners to Header Buttons
    attachHeaderButtonListeners();
}

// 11. Helper Functions

/**
 * Attaches event listeners to dynamically created header buttons
 */
function attachHeaderButtonListeners() {
    // Patient login/signup buttons
    const patientLoginBtn = document.getElementById("patientLogin");
    if (patientLoginBtn) {
        patientLoginBtn.addEventListener("click", function() {
            openModal('patientLogin');
        });
    }

    const patientSignupBtn = document.getElementById("patientSignup");
    if (patientSignupBtn) {
        patientSignupBtn.addEventListener("click", function() {
            openModal('patientSignup');
        });
    }

    // Add Doctor button (Admin only)
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", function() {
            openModal('addDoctor');
        });
    }

    // Home button (Doctor and Logged Patient)
    const homeBtn = document.getElementById("homeBtn");
    if (homeBtn) {
        homeBtn.addEventListener("click", function() {
            const role = localStorage.getItem("userRole");
            if (role === "doctor") {
                window.location.href = '/pages/doctorDashboard.html';
            } else if (role === "loggedPatient") {
                window.location.href = '/pages/loggedPatientDashboard.html';
            }
        });
    }

    // Appointments button (Logged Patient)
    const appointmentsBtn = document.getElementById("patientAppointments");
    if (appointmentsBtn) {
        appointmentsBtn.addEventListener("click", function() {
            window.location.href = '/pages/patientAppointments.html';
        });
    }

    // Logout links
    const logoutLinks = document.querySelectorAll('.logout-link');
    logoutLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const role = localStorage.getItem("userRole");
            if (role === "loggedPatient") {
                logoutPatient();
            } else {
                logout();
            }
        });
    });
}

/**
 * Logout function for admin and doctor roles
 */
function logout() {
    // Clear all session data
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    
    // Redirect to home page
    window.location.href = "/";
}

/**
 * Logout function for patient role - sets role back to "patient"
 */
function logoutPatient() {
    // Clear token but set role back to "patient" to show login/signup
    localStorage.removeItem("token");
    localStorage.removeItem("patientId");
    localStorage.setItem("userRole", "patient"); // Set back to patient (not logged in)
    
    // Redirect to patient dashboard or home page
    window.location.href = "/pages/patientDashboard.html";
}

/**
 * Opens a modal dialog
 * @param {string} modalId - The ID of the modal to open
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = "block";
        
        // Add close functionality
        const closeBtn = modal.querySelector('.close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                modal.style.display = 'none';
            });
        }
        
        // Close modal when clicking outside
        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        });
    } else {
        console.warn(`Modal with id '${modalId}' not found`);
    }
}

/**
 * Role selection handler (if needed for role switching)
 * @param {string} role - The selected role
 */
function selectRole(role) {
    localStorage.setItem("userRole", role);
    // Redirect based on role
    switch(role) {
        case 'doctor':
            window.location.href = '/pages/doctorDashboard.html';
            break;
        case 'admin':
            window.location.href = '/pages/adminDashboard.html';
            break;
        case 'patient':
            window.location.href = '/pages/patientDashboard.html';
            break;
        default:
            window.location.href = '/';
    }
}

// 12. Render the Header on Page Load
document.addEventListener('DOMContentLoaded', function() {
    renderHeader();
});

// Export functions if using modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { renderHeader, logout, logoutPatient, openModal };
}