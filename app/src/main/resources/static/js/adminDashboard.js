// adminDashboard.js

document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Dashboard loaded');
    loadDashboardData();
    loadDoctorCards();
});

// Load dashboard statistics
async function loadDashboardData() {
    try {
        // Fetch total doctors count
        const doctorsResponse = await fetch('/api/doctors');
        if (doctorsResponse.ok) {
            const doctors = await doctorsResponse.json();
            document.getElementById('totalDoctors').textContent = doctors.length;
        }

        // Fetch total patients count
        const patientsResponse = await fetch('/api/patients');
        if (patientsResponse.ok) {
            const patients = await patientsResponse.json();
            document.getElementById('totalPatients').textContent = patients.length;
        }

        // Fetch today's appointments count
        const appointmentsResponse = await fetch('/api/appointments');
        if (appointmentsResponse.ok) {
            const appointments = await appointmentsResponse.json();
            const today = new Date().toISOString().split('T')[0];
            const todayAppointments = appointments.filter(apt => 
                apt.appointmentTime && apt.appointmentTime.startsWith(today)
            );
            document.getElementById('todayAppointments').textContent = todayAppointments.length;
        }

        // Set pending actions (you can customize this logic)
        document.getElementById('pendingActions').textContent = '3';

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        // Set error states
        document.getElementById('totalDoctors').textContent = 'Error';
        document.getElementById('totalPatients').textContent = 'Error';
        document.getElementById('todayAppointments').textContent = 'Error';
        document.getElementById('pendingActions').textContent = 'Error';
    }
}

// Load doctor cards in the management section
async function loadDoctorCards() {
    try {
        const response = await fetch('/api/doctors');
        if (!response.ok) {
            throw new Error('Failed to fetch doctors');
        }
        
        const doctors = await response.json();
        const contentDiv = document.getElementById('content');
        
        if (!contentDiv) {
            console.error('Content div not found');
            return;
        }

        // Clear loading message
        contentDiv.innerHTML = '';

        if (doctors && doctors.length > 0) {
            doctors.forEach(doctor => {
                const doctorCard = createDoctorCard(doctor);
                contentDiv.appendChild(doctorCard);
            });
        } else {
            contentDiv.innerHTML = '<div class="no-data">No doctors found</div>';
        }
    } catch (error) {
        console.error('Error loading doctor cards:', error);
        const contentDiv = document.getElementById('content');
        if (contentDiv) {
            contentDiv.innerHTML = '<div class="error-message">Error loading doctors</div>';
        }
    }
}

// Create doctor card HTML
function createDoctorCard(doctor) {
    const card = document.createElement('div');
    card.className = 'doctor-card';
    card.innerHTML = `
        <div class="doctor-info">
            <h4>${doctor.name || 'Unknown Doctor'}</h4>
            <p><strong>Specialty:</strong> ${doctor.specialty || 'Not specified'}</p>
            <p><strong>Email:</strong> ${doctor.email || 'No email'}</p>
            <p><strong>Phone:</strong> ${doctor.phone || 'No phone'}</p>
            <p><strong>Available Times:</strong> ${doctor.availableTimes ? doctor.availableTimes.join(', ') : 'Not specified'}</p>
        </div>
        <div class="doctor-actions">
            <button class="btn-edit" onclick="editDoctor(${doctor.id})">Edit</button>
            <button class="btn-delete" onclick="deleteDoctor(${doctor.id})">Delete</button>
        </div>
    `;
    return card;
}

// Action button functions
window.showAddDoctorForm = function() {
    alert('Add Doctor functionality would open here');
    // You can implement a modal or redirect to add doctor page
};

window.showAddPatientForm = function() {
    alert('Add Patient functionality would open here');
};

window.generateReports = function() {
    alert('Report generation would start here');
};

window.viewSystemLogs = function() {
    alert('System logs would display here');
};

// Search and filter functions
window.handleSearch = function() {
    const searchTerm = document.getElementById('searchBar').value.toLowerCase();
    const doctorCards = document.querySelectorAll('.doctor-card');
    
    doctorCards.forEach(card => {
        const doctorName = card.querySelector('h4').textContent.toLowerCase();
        const doctorSpecialty = card.querySelector('p:nth-child(2)').textContent.toLowerCase();
        
        if (doctorName.includes(searchTerm) || doctorSpecialty.includes(searchTerm)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
};

window.applyFilters = function() {
    const specialtyFilter = document.getElementById('specialtyFilter').value;
    const doctorCards = document.querySelectorAll('.doctor-card');
    
    doctorCards.forEach(card => {
        const doctorSpecialty = card.querySelector('p:nth-child(2)').textContent;
        
        if (!specialtyFilter || doctorSpecialty.includes(specialtyFilter)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
};

// Doctor management functions
window.editDoctor = function(doctorId) {
    alert(`Edit doctor with ID: ${doctorId}`);
    // Implement edit functionality
};

window.deleteDoctor = function(doctorId) {
    if (confirm('Are you sure you want to delete this doctor?')) {
        fetch(`/api/doctors/${doctorId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                alert('Doctor deleted successfully');
                loadDoctorCards(); // Refresh the list
                loadDashboardData(); // Refresh stats
            } else {
                alert('Failed to delete doctor');
            }
        })
        .catch(error => {
            console.error('Error deleting doctor:', error);
            alert('Error deleting doctor');
        });
    }
};

// Modal functions
window.closeModal = function() {
    document.getElementById('modal-overlay').style.display = 'none';
};

// Make sure these functions are available globally
window.loadDashboardData = loadDashboardData;
window.loadDoctorCards = loadDoctorCards;