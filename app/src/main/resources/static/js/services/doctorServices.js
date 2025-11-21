// doctorServices.js

// Import API Base URL
import { API_BASE_URL } from "../config/config.js";

// Set Doctor API Endpoint
const DOCTOR_API = `${API_BASE_URL}/doctor`;

/**
 * Create a Function to Get All Doctors
 * Fetches all doctors from the API
 * @returns {Promise<Array>} Array of doctor objects or empty array on error
 */
export async function getDoctors() {
    try {
        // Send GET request to the doctor endpoint
        const response = await fetch(DOCTOR_API, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        // Check if response is successful
        if (response.ok) {
            // Extract and return the list of doctors from the response JSON
            const doctors = await response.json();
            return doctors;
        } else {
            console.error('Failed to fetch doctors:', response.status);
            return [];
        }
    } catch (error) {
        // Handle any errors using try-catch block
        console.error('Error fetching doctors:', error);
        // Return an empty list if something goes wrong to avoid breaking the frontend
        return [];
    }
}

/**
 * Create a Function to Delete a Doctor
 * Deletes a doctor by ID with authentication
 * @param {number|string} id - The doctor's unique ID
 * @param {string} token - Authentication token for security
 * @returns {Promise<Object>} Response with success status and message
 */
export async function deleteDoctor(id, token) {
    try {
        // Construct the full endpoint URL using the ID
        const url = `${DOCTOR_API}/${id}`;

        // Send DELETE request to that endpoint
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        // Parse the JSON response
        const result = await response.json();

        // Return a structured response with success and message
        if (response.ok) {
            return {
                success: true,
                message: result.message || 'Doctor deleted successfully'
            };
        } else {
            return {
                success: false,
                message: result.message || 'Failed to delete doctor'
            };
        }
    } catch (error) {
        // Catch and handle any errors to prevent frontend crashes
        console.error('Error deleting doctor:', error);
        return {
            success: false,
            message: 'Network error occurred while deleting doctor'
        };
    }
}

/**
 * Create a Function to Save (Add) a New Doctor
 * Saves a new doctor to the system with authentication
 * @param {Object} doctor - Doctor object containing all details
 * @param {string} token - Authentication token for Admin
 * @returns {Promise<Object>} Response with success status and message
 */
export async function saveDoctor(doctor, token) {
    try {
        // Send POST request with headers specifying JSON data
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            // Include the doctor data in the request body (converted to JSON)
            body: JSON.stringify(doctor)
        });

        const result = await response.json();

        // Return a structured response with success and message
        if (response.ok) {
            return {
                success: true,
                message: result.message || 'Doctor saved successfully',
                data: result.data || null
            };
        } else {
            return {
                success: false,
                message: result.message || 'Failed to save doctor'
            };
        }
    } catch (error) {
        // Catch and log any errors to help during debugging
        console.error('Error saving doctor:', error);
        return {
            success: false,
            message: 'Network error occurred while saving doctor'
        };
    }
}

/**
 * Create a Function to Filter Doctors
 * Filters doctors based on name, time, and specialty
 * @param {string} name - Doctor name to filter by
 * @param {string} time - Available time to filter by
 * @param {string} specialty - Specialty to filter by
 * @returns {Promise<Array>} Filtered list of doctors or empty array
 */
export async function filterDoctors(name = '', time = '', specialty = '') {
    try {
        // Construct query parameters
        const params = new URLSearchParams();
        if (name) params.append('name', name);
        if (time) params.append('time', time);
        if (specialty) params.append('specialty', specialty);

        // Construct GET request URL with query parameters
        const url = `${DOCTOR_API}/filter?${params.toString()}`;

        // Send GET request to retrieve matching doctor records
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const doctors = await response.json();
            // Return the filtered list of doctors
            return doctors;
        } else {
            console.error('Failed to filter doctors:', response.status);
            return [];
        }
    } catch (error) {
        // Handle cases where no filters are applied or network errors
        console.error('Error filtering doctors:', error);
        return [];
    }
}

/**
 * Get doctor by ID
 * @param {number|string} id - Doctor ID
 * @returns {Promise<Object>} Doctor object or null
 */
export async function getDoctorById(id) {
    try {
        const response = await fetch(`${DOCTOR_API}/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            return await response.json();
        } else {
            console.error('Failed to fetch doctor:', response.status);
            return null;
        }
    } catch (error) {
        console.error('Error fetching doctor by ID:', error);
        return null;
    }
}

/**
 * Update doctor information
 * @param {number|string} id - Doctor ID
 * @param {Object} doctor - Updated doctor data
 * @param {string} token - Authentication token
 * @returns {Promise<Object>} Response with success status
 */
export async function updateDoctor(id, doctor, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(doctor)
        });

        const result = await response.json();

        if (response.ok) {
            return {
                success: true,
                message: result.message || 'Doctor updated successfully',
                data: result.data || null
            };
        } else {
            return {
                success: false,
                message: result.message || 'Failed to update doctor'
            };
        }
    } catch (error) {
        console.error('Error updating doctor:', error);
        return {
            success: false,
            message: 'Network error occurred while updating doctor'
        };
    }
}