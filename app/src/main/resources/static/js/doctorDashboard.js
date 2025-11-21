// doctorDashboard.js

// Import Required Modules
import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

// Initialize Global Variables
const tableBody = document.getElementById('patientTableBody');
let selectedDate = new Date().toISOString().split('T')[0]; // Today's date in 'YYYY-MM-DD' format
const token = localStorage.getItem('token'); // Authentication token
let patientName = null; // For search filtering

// Setup Search Bar Functionality
document.getElementById('searchBar').addEventListener('input', function(event) {
    // Trim and check the input value
    const searchValue = event.target.value.trim();
    
    // If not empty, use it as the patientName for filtering
    if (searchValue) {
        patientName = searchValue;
    } else {
        // Else, reset patientName to "null" (as expected by backend)
        patientName = null;
    }
    
    // Reload the appointments list with the updated filter
    loadAppointments();
});

// Bind Event Listeners to Filter Controls

// "Today's Appointments" button
document.getElementById('todayButton').addEventListener('click', function() {
    // Set selectedDate to today's date
    selectedDate = new Date().toISOString().split('T')[0];
    
    // Update the date picker UI to match
    document.getElementById('datePicker').value = selectedDate;
    
    // Reload the appointments for today
    loadAppointments();
});

// Date picker
document.getElementById('datePicker').addEventListener('change', function(event) {
    // Update the selectedDate variable when changed
    selectedDate = event.target.value;
    
    // Reload the appointments for that specific date
    loadAppointments();
});

/**
 * Function: loadAppointments
 * Purpose: Fetch and display appointments based on selected date and optional patient name
 */
async function loadAppointments() {
    try {
        // Step 1: Call getAllAppointments with selectedDate, patientName, and token
        const appointments = await getAllAppointments(selectedDate, patientName, token);
        
        // Step 2: Clear the table body content before rendering new rows
        tableBody.innerHTML = '';
        
        // Step 3: If no appointments are returned:
        if (!appointments || appointments.length === 0) {
            const noAppointmentsRow = document.createElement('tr');
            noAppointmentsRow.innerHTML = `
                <td colspan="6" class="no-appointments">
                    No appointments found for ${selectedDate === new Date().toISOString().split('T')[0] ? 'today' : 'selected date'}.
                </td>
            `;
            tableBody.appendChild(noAppointmentsRow);
            return;
        }
        
        // Step 4: If appointments exist:
        appointments.forEach(appointment => {
            // Construct a 'patient' object with id, name, phone, and email
            const patient = {
                id: appointment.patientId || appointment.id,
                name: appointment.patientName || 'Unknown Patient',
                phone: appointment.patientPhone || 'N/A',
                email: appointment.patientEmail || 'N/A',
                appointmentTime: appointment.appointmentTime,
                status: appointment.status || 'scheduled'
            };
            
            // Call createPatientRow to generate a table row for the appointment
            const patientRow = createPatientRow(patient, appointment);
            
            // Append each row to the table body
            tableBody.appendChild(patientRow);
        });
        
    } catch (error) {
        // Step 5: Catch and handle any errors during fetch:
        console.error('Error loading appointments:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="error-message">
                    Error loading appointments. Please try again later.
                </td>
            </tr>
        `;
    }
}

/**
 * Function to update appointment status
 */
window.updateAppointmentStatus = async function(appointmentId, newStatus) {
    try {
        const response = await fetch(`/api/appointments/${appointmentId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ status: newStatus })
        });

        if (response.ok) {
            alert('Appointment status updated successfully');
            loadAppointments(); // Refresh the list
        } else {
            throw new Error('Failed to update appointment status');
        }
    } catch (error) {
        console.error('Error updating appointment status:', error);
        alert('Error updating appointment status. Please try again.');
    }
}

/**
 * Function to add consultation notes
 */
window.addConsultationNotes = async function(appointmentId) {
    const notes = prompt('Enter consultation notes:');
    if (notes === null) return; // User cancelled
    
    try {
        const response = await fetch(`/api/appointments/${appointmentId}/notes`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ notes: notes })
        });

        if (response.ok) {
            alert('Consultation notes added successfully');
        } else {
            throw new Error('Failed to add consultation notes');
        }
    } catch (error) {
        console.error('Error adding consultation notes:', error);
        alert('Error adding consultation notes. Please try again.');
    }
}

// Initial Render on Page Load
document.addEventListener('DOMContentLoaded', function() {
    // Set initial date picker value
    document.getElementById('datePicker').value = selectedDate;
    
    // Call loadAppointments() to load today's appointments by default
    loadAppointments();
    
    // Optional: Set up periodic refresh (every 5 minutes)
    setInterval(loadAppointments, 5 * 60 * 1000);
});

// Export for testing purposes
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { loadAppointments, updateAppointmentStatus, addConsultationNotes };
}