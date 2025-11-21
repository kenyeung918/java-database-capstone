// patientDashboard.js
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { openModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';
import { patientSignup, patientLogin } from './services/patientServices.js';

// Combine all DOMContentLoaded event listeners
document.addEventListener("DOMContentLoaded", () => {
    loadDoctorCards();
    bindModalTriggers();
    setupSearchAndFilter();
});

/**
 * Bind Modal Triggers for Login and Signup
 */
function bindModalTriggers() {
    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => openModal("patientLogin"));
    }
}

/**
 * Setup Search and Filter Event Listeners
 */
function setupSearchAndFilter() {
    document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
    document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
    document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
}

/**
 * Load Doctor Cards on Page Load
 */
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to load doctors:", error);
        const contentDiv = document.getElementById("content");
        if (contentDiv) {
            contentDiv.innerHTML = "<p class='error-message'>Failed to load doctors. Please try again later.</p>";
        }
    }
}

/**
 * Filter Doctors Based on Search and Filter Inputs
 */
async function filterDoctorsOnChange() {
    try {
        const searchBar = document.getElementById("searchBar").value.trim();
        const filterTime = document.getElementById("filterTime").value;
        const filterSpecialty = document.getElementById("filterSpecialty").value;

        const name = searchBar.length > 0 ? searchBar : null;
        const time = filterTime.length > 0 ? filterTime : null;
        const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

        const doctors = await filterDoctors(name, time, specialty);
        renderDoctorCards(doctors);
        
    } catch (error) {
        console.error("Failed to filter doctors:", error);
        alert("❌ An error occurred while filtering doctors.");
    }
}

/**
 * Render Utility - Render a given list of doctors
 * @param {Array} doctors - Array of doctor objects
 */
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) {
        console.error("Content div not found");
        return;
    }

    contentDiv.innerHTML = "";

    if (doctors && doctors.length > 0) {
        doctors.forEach(doctor => {
            const card = createDoctorCard(doctor);
            contentDiv.appendChild(card);
        });
    } else {
        contentDiv.innerHTML = "<p class='no-doctors'>No doctors found with the given filters.</p>";
    }
}

/**
 * Handle Patient Signup
 */
window.signupPatient = async function () {
    try {
        // Get form values - fixed IDs to match typical form structure
        const name = document.getElementById("signupName")?.value || document.getElementById("name")?.value;
        const email = document.getElementById("signupEmail")?.value || document.getElementById("email")?.value;
        const password = document.getElementById("signupPassword")?.value || document.getElementById("password")?.value;
        const phone = document.getElementById("signupPhone")?.value || document.getElementById("phone")?.value;
        const address = document.getElementById("signupAddress")?.value || document.getElementById("address")?.value;

        // Validate required fields
        if (!name || !email || !password || !phone) {
            alert('Please fill in all required fields (Name, Email, Password, Phone).');
            return;
        }

        const data = { name, email, password, phone, address };
        const { success, message } = await patientSignup(data);
        
        if (success) {
            alert(message);
            closeModal('patientSignup');
            // Clear form
            const form = document.querySelector('#patientSignup form');
            if (form) form.reset();
        } else {
            alert(message);
        }
    } catch (error) {
        console.error("Signup failed:", error);
        alert("❌ An error occurred while signing up.");
    }
};

/**
 * Handle Patient Login
 */
window.loginPatient = async function () {
    try {
        // Get form values - fixed IDs to match typical form structure
        const email = document.getElementById("loginEmail")?.value || document.getElementById("email")?.value;
        const password = document.getElementById("loginPassword")?.value || document.getElementById("password")?.value;

        if (!email || !password) {
            alert('Please enter both email and password.');
            return;
        }

        const data = { email, password };
        console.log("loginPatient :: ", data);
        
        const response = await patientLogin(data);
        console.log("Status Code:", response.status);
        console.log("Response OK:", response.ok);
        
        if (response.ok) {
            const result = await response.json();
            console.log(result);
            
            // Store token and role
            localStorage.setItem('token', result.token);
            selectRole('loggedPatient');
            
            // Redirect to logged patient dashboard
            window.location.href = '/pages/loggedPatientDashboard.html';
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || '❌ Invalid credentials!');
        }
    } catch (error) {
        console.error("Error :: loginPatient :: ", error);
        alert("❌ Failed to Login. Please try again.");
    }
};

/**
 * Role selection helper function
 */
function selectRole(role) {
    localStorage.setItem('userRole', role);
    console.log(`User role set to: ${role}`);
}

/**
 * Close modal function
 */
window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
};

/**
 * Handle modal close when clicking outside
 */
document.addEventListener('click', function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});

// Export for testing or module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { 
        loadDoctorCards, 
        filterDoctorsOnChange, 
        renderDoctorCards,
        signupPatient: window.signupPatient,
        loginPatient: window.loginPatient
    };
}