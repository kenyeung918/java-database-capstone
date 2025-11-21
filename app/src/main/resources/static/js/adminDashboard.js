// adminDashboard.js

// Import Required Modules
import { openModal } from '../components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// Event Binding
document.addEventListener('DOMContentLoaded', function() {
    // Load doctors when page loads
    loadDoctorCards();
    
    // Add Doctor button event listener
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor');
        });
    }

    // Search and Filter event listeners
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    if (searchBar) {
        searchBar.addEventListener("input", filterDoctorsOnChange);
    }
    if (filterTime) {
        filterTime.addEventListener("change", filterDoctorsOnChange);
    }
    if (filterSpecialty) {
        filterSpecialty.addEventListener("change", filterDoctorsOnChange);
    }
});

/**
 * Function: loadDoctorCards
 * Purpose: Fetch all doctors and display them as cards
 */
async function loadDoctorCards() {
    try {
        // Call getDoctors() from the service layer
        const doctors = await getDoctors();
        
        // Clear the current content area
        const contentDiv = document.getElementById("content");
        if (!contentDiv) {
            console.error("Content div not found");
            return;
        }
        contentDiv.innerHTML = "";
        
        // For each doctor returned:
        if (doctors && doctors.length > 0) {
            doctors.forEach(doctor => {
                // Create a doctor card using createDoctorCard()
                const doctorCard = createDoctorCard(doctor);
                // Append it to the content div
                contentDiv.appendChild(doctorCard);
            });
        } else {
            contentDiv.innerHTML = '<p class="no-doctors">No doctors found</p>';
        }
    } catch (error) {
        // Handle any fetch errors by logging them
        console.error("Error loading doctor cards:", error);
        const contentDiv = document.getElementById("content");
        if (contentDiv) {
            contentDiv.innerHTML = '<p class="error-message">Error loading doctors. Please try again.</p>';
        }
    }
}

/**
 * Function: filterDoctorsOnChange
 * Purpose: Filter doctors based on name, available time, and specialty
 */
async function filterDoctorsOnChange() {
    try {
        // Read values from the search bar and filters
        const name = document.getElementById("searchBar")?.value || '';
        const time = document.getElementById("filterTime")?.value || '';
        const specialty = document.getElementById("filterSpecialty")?.value || '';
        
        // Normalize empty values to null
        const searchName = name.trim() || null;
        const searchTime = time || null;
        const searchSpecialty = specialty || null;
        
        // Call filterDoctors(name, time, specialty) from the service
        const doctors = await filterDoctors(searchName, searchTime, searchSpecialty);
        
        // If doctors are found:
        if (doctors && doctors.length > 0) {
            // Render them using createDoctorCard()
            renderDoctorCards(doctors);
        } else {
            // If no doctors match the filter:
            // Show a message: "No doctors found with the given filters."
            const contentDiv = document.getElementById("content");
            if (contentDiv) {
                contentDiv.innerHTML = '<p class="no-doctors">No doctors found with the given filters.</p>';
            }
        }
    } catch (error) {
        // Catch and display any errors with an alert
        console.error("Error filtering doctors:", error);
        alert("Error filtering doctors. Please try again.");
    }
}

/**
 * Function: renderDoctorCards
 * Purpose: A helper function to render a list of doctors passed to it
 * @param {Array} doctors - Array of doctor objects to render
 */
function renderDoctorCards(doctors) {
    // Clear the content area
    const contentDiv = document.getElementById("content");
    if (!contentDiv) {
        console.error("Content div not found");
        return;
    }
    contentDiv.innerHTML = "";
    
    // Loop through the doctors and append each card to the content area
    if (doctors && doctors.length > 0) {
        doctors.forEach(doctor => {
            const doctorCard = createDoctorCard(doctor);
            contentDiv.appendChild(doctorCard);
        });
    } else {
        contentDiv.innerHTML = '<p class="no-doctors">No doctors found</p>';
    }
}

/**
 * Function: adminAddDoctor
 * Purpose: Collect form data and add a new doctor to the system
 */
window.adminAddDoctor = async function() {
    try {
        // Collect input values from the modal form
        const name = document.getElementById('doctorName')?.value.trim();
        const email = document.getElementById('doctorEmail')?.value.trim();
        const phone = document.getElementById('doctorPhone')?.value.trim();
        const password = document.getElementById('doctorPassword')?.value;
        const specialty = document.getElementById('doctorSpecialty')?.value.trim();
        
        // Collect available times from checkboxes
        const availableTimes = [];
        const timeCheckboxes = document.querySelectorAll('input[name="availableTimes"]:checked');
        timeCheckboxes.forEach(checkbox => {
            availableTimes.push(checkbox.value);
        });

        // Validate required fields
        if (!name || !email || !phone || !password || !specialty || availableTimes.length === 0) {
            alert('Please fill in all required fields and select at least one available time.');
            return;
        }

        // Validate email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert('Please enter a valid email address.');
            return;
        }

        // Retrieve the authentication token from localStorage
        const token = localStorage.getItem('token');
        if (!token) {
            // If no token is found, show an alert and stop execution
            alert('Admin authentication required. Please log in again.');
            return;
        }

        // Build a doctor object with the form values
        const doctor = {
            name,
            email,
            phone,
            password,
            specialty,
            availableTimes
        };

        // Call saveDoctor(doctor, token) from the service
        const result = await saveDoctor(doctor, token);

        // If save is successful:
        if (result.success) {
            // Show a success message
            alert(result.message || 'Doctor added successfully!');
            
            // Close the modal
            const modal = document.getElementById('addDoctor');
            if (modal) {
                modal.style.display = 'none';
            }
            
            // Clear the form
            const form = document.getElementById('addDoctorForm');
            if (form) {
                form.reset();
            }
            
            // Reload the doctor list
            await loadDoctorCards();
            
        } else {
            // If saving fails, show an error message
            alert(result.message || 'Failed to add doctor. Please try again.');
        }
    } catch (error) {
        console.error('Error adding doctor:', error);
        alert('An unexpected error occurred. Please try again.');
    }
}

/**
 * Function to close modal
 */
window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Function to handle modal close when clicking outside
 */
document.addEventListener('click', function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});