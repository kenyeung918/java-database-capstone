/*
  Function to render the footer content into the page
  This section dynamically generates the footer content for the web page, including the hospital's logo, copyright information, and various helpful links.
*/

function renderFooter() {
    // Select the footer element from the DOM
    const footer = document.getElementById("footer");
    
    // Set the inner HTML of the footer element to include the footer content
    footer.innerHTML = `
        <!-- 2. Create the Footer Wrapper -->
        <footer class="footer">
            
            <!-- 3. Create the Footer Container -->
            <div class="footer-container">
                
                <!-- 4. Add the Hospital Logo and Copyright Info -->
                <div class="footer-logo">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo">
                    <p>© Copyright 2025. All Rights Reserved by Hospital CMS.</p>
                </div>
                
                <!-- 5. Create the Links Section -->
                <div class="footer-links">
                    
                    <!-- 6. Add the 'Company' Links Column -->
                    <div class="footer-column">
                        <h4>Company</h4>
                        <a href="#">About</a>
                        <a href="#">Careers</a>
                        <a href="#">Press</a>
                    </div>
                    
                    <!-- 7. Add the 'Support' Links Column -->
                    <div class="footer-column">
                        <h4>Support</h4>
                        <a href="#">Account</a>
                        <a href="#">Help Center</a>
                        <a href="#">Contact Us</a>
                    </div>
                    
                    <!-- 8. Add the 'Legals' Links Column -->
                    <div class="footer-column">
                        <h4>Legals</h4>
                        <a href="#">Terms & Conditions</a>
                        <a href="#">Privacy Policy</a>
                        <a href="#">Licensing</a>
                    </div>
                    
                </div>
                
            </div> <!-- 9. Close the Footer Container -->
            
        </footer> <!-- 10. Close the Footer Element -->
    `;
    
    // 11. Footer Rendering Complete
    console.log("Footer rendered successfully");
}

// Call the renderFooter function to populate the footer in the page
renderFooter();