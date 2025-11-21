// Import the openModal function to handle showing login popups/modals
import { openModal } from '../components/modals.js';

// Import the base API URL from the config file
import { API_BASE_URL } from '../config/config.js';

// Define constants for the admin and doctor login API endpoints using the base URL
const ADMIN_API = `${API_BASE_URL}/admin`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;

// Use DOMContentLoaded instead of window.onload for better performance
document.addEventListener('DOMContentLoaded', function () {
    // Select the "adminLogin" and "doctorLogin" buttons using getElementById
    const adminLoginBtn = document.getElementById('adminLogin');
    const doctorLoginBtn = document.getElementById('doctorLogin');

    // If the admin login button exists:
    if (adminLoginBtn) {
        // Add a click event listener that calls openModal('adminLogin') to show the admin login modal
        adminLoginBtn.addEventListener('click', () => {
            openModal('adminLogin');
        });
    }

    // If the doctor login button exists:
    if (doctorLoginBtn) {
        // Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal
        doctorLoginBtn.addEventListener('click', () => {
            openModal('doctorLogin');
        });
    }
});

// Define a function named adminLoginHandler on the global window object
window.adminLoginHandler = async function () {
    try {
        // Step 1: Get the entered username and password from the input fields
        const username = document.getElementById('adminUsername').value.trim();
        const password = document.getElementById('adminPassword').value;

        // Validate inputs
        if (!username || !password) {
            alert('Please enter both username and password.');
            return;
        }

        // Step 2: Create an admin object with these credentials
        const admin = { username, password };

        // Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(admin)
        });

        // Step 4: If the response is successful:
        if (response.ok) {
            // Parse the JSON response to get the token
            const data = await response.json();
            
            // Store the token in localStorage
            localStorage.setItem('token', data.token);
            
            // Call selectRole('admin') to proceed with admin-specific behavior
            selectRole('admin');
            
        } else {
            // Step 5: If login fails or credentials are invalid:
            // Show an alert with an error message
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || 'Invalid admin credentials! Please try again.');
        }
    } catch (error) {
        // Step 6: Wrap everything in a try-catch to handle network or server errors
        console.error('Admin login error:', error);
        // Show a generic error message if something goes wrong
        alert('Network error occurred during admin login. Please check your connection and try again.');
    }
};

// Define a function named doctorLoginHandler on the global window object
window.doctorLoginHandler = async function () {
    try {
        // Step 1: Get the entered email and password from the input fields
        const email = document.getElementById('doctorEmail').value.trim();
        const password = document.getElementById('doctorPassword').value;

        // Validate inputs
        if (!email || !password) {
            alert('Please enter both email and password.');
            return;
        }

        // Step 2: Create a doctor object with these credentials
        const doctor = { email, password };

        // Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(doctor)
        });

        // Step 4: If login is successful:
        if (response.ok) {
            // Parse the JSON response to get the token
            const data = await response.json();
            
            // Store the token in localStorage
            localStorage.setItem('token', data.token);
            
            // Call selectRole('doctor') to proceed with doctor-specific behavior
            selectRole('doctor');
            
        } else {
            // Step 5: If login fails:
            // Show an alert for invalid credentials
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || 'Invalid doctor credentials! Please try again.');
        }
    } catch (error) {
        // Step 6: Wrap in a try-catch block to handle errors gracefully
        // Log the error to the console
        console.error('Doctor login error:', error);
        // Show a generic error message
        alert('Network error occurred during doctor login. Please check your connection and try again.');
    }
};

// Helper function to select and store user role
function selectRole(role) {
    localStorage.setItem('userRole', role);
    console.log(`User role set to: ${role}`);
    
    // Redirect to appropriate dashboard based on role
    switch (role) {
        case 'admin':
            window.location.href = '/pages/adminDashboard.html';
            break;
        case 'doctor':
            window.location.href = '/pages/doctorDashboard.html';
            break;
        default:
            console.log('Unknown role:', role);
            window.location.href = '/';
    }
}