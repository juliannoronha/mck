/* =============================================================================
 * Wellca Management Module
 * 
 * PURPOSE: Handles tab navigation, form submissions, and dynamic calculations
 * for the Wellca management system
 * ============================================================================= */

let messageContainer;

document.addEventListener('DOMContentLoaded', function() {
    // Create message container if it doesn't exist
    if (!messageContainer) {
        messageContainer = document.createElement('div');
        messageContainer.className = 'message-container';
        document.body.appendChild(messageContainer);
    }
    
    // Initialize tab functionality
    initializeTabs();
    
    // Initialize date picker with today's date
    document.getElementById('date').valueAsDate = new Date();
    
    // Setup form submissions and calculations
    setupFormHandlers();
    
    // Setup delivery form submission
    setupDeliveryForm();
    setupRxSalesForm();
    setupProfilesForm();
    setupServicesForm();
});

/* ------------------------------------------------------------------------- 
 * Tab Navigation Functions
 * --------------------------------------------------------------------- */
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabPanes = document.querySelectorAll('.tab-pane');
    
    // Hide all tabs except the first one
    tabPanes.forEach((pane, index) => {
        if (index !== 0) {
            pane.style.display = 'none';
        }
    });

    // Add click handlers to tab buttons
    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            // Remove active class from all buttons and panes
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabPanes.forEach(pane => pane.style.display = 'none');

            // Add active class to clicked button
            button.classList.add('active');

            // Show corresponding pane
            const tabId = button.getAttribute('data-tab');
            const pane = document.getElementById(tabId);
            if (pane) {
                pane.style.display = 'block';
                
                // If switching to reports tab, refresh the data
                if (tabId === 'reports') {
                    refreshReportData();
                }
            }
        });
    });
}

/* ------------------------------------------------------------------------- 
 * Form Setup and Handlers
 * --------------------------------------------------------------------- */
function setupFormHandlers() {
    // Setup delivery form calculations
    setupDeliveryCalculations();
    
    // Setup RX sales calculations
    setupRxCalculations();
    
    // Setup profiles calculations
    setupProfilesCalculations();
    
    // Setup services form
    setupServicesForm();
}

function setupDeliveryCalculations() {
    const deliveryInputs = ['purolator', 'fedex', 'oneCourier', 'goBolt'];
    
    deliveryInputs.forEach(id => {
        document.getElementById(id)?.addEventListener('input', () => {
            const total = deliveryInputs
                .map(input => parseInt(document.getElementById(input)?.value) || 0)
                .reduce((sum, current) => sum + current, 0);
            
            document.getElementById('totalDeliveries').textContent = total;
        });
    });
}

function setupRxCalculations() {
    const rxInputs = ['newRx', 'refill', 'reAuth', 'hold'];
    
    rxInputs.forEach(id => {
        document.getElementById(id)?.addEventListener('input', () => {
            calculateRxTotals();
        });
    });
}

function calculateRxTotals() {
    const newRx = parseInt(document.getElementById('newRx')?.value) || 0;
    const refill = parseInt(document.getElementById('refill')?.value) || 0;
    const reAuth = parseInt(document.getElementById('reAuth')?.value) || 0;
    const hold = parseInt(document.getElementById('hold')?.value) || 0;

    const totalFilled = newRx + refill + reAuth;
    const totalEntered = totalFilled + hold;

    document.getElementById('totalFilled').textContent = totalFilled;
    document.getElementById('totalEntered').textContent = totalEntered;
    
    // Calculate per hour (assuming 8-hour workday)
    const perHour = (totalEntered / 8).toFixed(2);
    document.getElementById('totalPerHour').textContent = perHour;
}

function setupProfilesCalculations() {
    ['profilesEntered', 'whoFilledRx'].forEach(id => {
        document.getElementById(id)?.addEventListener('input', calculateActivePercentage);
    });
}

function calculateActivePercentage() {
    const profilesEntered = parseInt(document.getElementById('profilesEntered')?.value) || 0;
    const whoFilledRx = parseInt(document.getElementById('whoFilledRx')?.value) || 0;
    
    if (profilesEntered > 0) {
        const percentage = (whoFilledRx / profilesEntered * 100).toFixed(2);
        document.getElementById('activePercentage').value = percentage;
    }
}

function setupServicesForm() {
    const servicesForm = document.getElementById('servicesForm');
    if (servicesForm) {
        // Remove any existing event listeners to prevent double submissions
        const clonedForm = servicesForm.cloneNode(true);
        servicesForm.parentNode.replaceChild(clonedForm, servicesForm);
        
        clonedForm.addEventListener('submit', async (e) => {
            e.preventDefault(); // Prevent default form submission
            e.stopPropagation(); // Stop event bubbling
            
            // Disable submit button to prevent double clicks
            const submitButton = clonedForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }

            console.log('Submitting Professional Services form...');

            try {
                // Get form values
                const serviceType = document.getElementById('serviceType').value;
                const serviceCost = parseFloat(document.getElementById('serviceCost').value) || 0;

                if (!serviceType.trim()) {
                    showMessage('Please enter a service type', 'error');
                    return;
                }

                const formData = {
                    date: document.getElementById('date').value,
                    // Professional Services data
                    serviceType: serviceType,
                    serviceCost: serviceCost,
                    // Initialize other fields to 0
                    purolator: 0,
                    fedex: 0,
                    oneCourier: 0,
                    goBolt: 0,
                    newRx: 0,
                    refill: 0,
                    reAuth: 0,
                    hold: 0,
                    profilesEntered: 0,
                    whoFilledRx: 0,
                    activePercentage: 0
                };

                console.log('Professional Services form data:', formData);

                const response = await submitForm(formData);
                console.log('Professional Services submission response:', response);
                showMessage('Successfully Submitted!');

                // Update services summary if elements exist
                if (document.getElementById('totalServices')) {
                    const currentTotal = parseFloat(document.getElementById('totalServices').textContent) || 0;
                    document.getElementById('totalServices').textContent = 
                        (currentTotal + serviceCost).toFixed(2);
                }

                // Re-enable submit button after successful submission
                if (submitButton) {
                    submitButton.disabled = false;
                }
                
                // Reset the form after successful submission
                clonedForm.reset();

            } catch (error) {
                console.error('Error submitting Professional Services data:', error);
                showMessage('Failed to save Professional Services data: ' + error.message, 'error');
                
                // Re-enable submit button on error
                if (submitButton) {
                    submitButton.disabled = false;
                }
            }
        });
    } else {
        console.error('Services form not found in DOM');
    }
}

/* ------------------------------------------------------------------------- 
 * Form Submission Handlers
 * --------------------------------------------------------------------- */
async function handleServiceSubmission(e) {
    e.preventDefault();
    
    const serviceData = {
        date: document.getElementById('date').value,
        serviceType: document.getElementById('serviceType').value,
        serviceCost: parseFloat(document.getElementById('serviceCost').value)
    };

    try {
        const response = await submitServiceData(serviceData);
        if (response.ok) {
            showSuccessMessage('Service added successfully');
            updateServicesList();
        } else {
            showErrorMessage('Error adding service');
        }
    } catch (error) {
        showErrorMessage('Error: ' + error.message);
    }
}

/* ------------------------------------------------------------------------- 
 * Utility Functions
 * --------------------------------------------------------------------- */
function showSuccessMessage(message) {
    const successDiv = document.getElementById('successMessage');
    if (successDiv) {
        successDiv.textContent = message;
        successDiv.style.display = 'block';
        setTimeout(() => successDiv.style.display = 'none', 3000);
    }
}

function showErrorMessage(message) {
    const errorDiv = document.getElementById('errorMessage');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        setTimeout(() => errorDiv.style.display = 'none', 3000);
    }
}

/* ------------------------------------------------------------------------- 
 * API Calls
 * --------------------------------------------------------------------- */
async function submitServiceData(data) {
    return fetch('/wellca-management/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(data)
    });
}

async function refreshReportData() {
    try {
        console.log('Refreshing report data');
        
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        
        if (!startDateInput.value || !endDateInput.value) {
            showErrorMessage('Please select both start and end dates');
            return;
        }

        // Format dates to ISO format (YYYY-MM-DD)
        const startDate = new Date(startDateInput.value).toISOString().split('T')[0];
        const endDate = new Date(endDateInput.value).toISOString().split('T')[0];
        
        console.log('Date range:', startDate, 'to', endDate);

        const response = await fetch(`/wellca-management/range?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        
        if (!response.ok) {
            const errorData = await response.text();
            console.error('Server response:', errorData);
            throw new Error('Failed to fetch report data');
        }

        const data = await response.json();
        console.log('Report data:', data);
        
        updateReportDisplay(data);
    } catch (error) {
        console.error('Error refreshing report data:', error);
        showErrorMessage(`Error loading report: ${error.message}`);
    }
}

async function submitForm(formData) {
    try {
        console.log('Submitting form data:', formData);
        
        const response = await fetch('/wellca-management/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(formData)
        });

        console.log('Response status:', response.status);
        
        if (!response.ok) {
            const errorData = await response.json();
            console.error('Submission error:', errorData);
            throw new Error(`Submission failed: ${JSON.stringify(errorData)}`);
        }

        const data = await response.json();
        console.log('Submission successful:', data);
        
        return data;
    } catch (error) {
        console.error('Error submitting form:', error);
        throw error;
    }
}

// Add date validation function
function validateDateRange() {
    const startDate = new Date(document.getElementById('startDate').value);
    const endDate = new Date(document.getElementById('endDate').value);
    
    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        showErrorMessage('Please select valid dates');
        return false;
    }
    
    if (endDate < startDate) {
        showErrorMessage('End date must be after start date');
        return false;
    }
    
    return true;
}

async function generateReport() {
    console.log('Generating report...');
    
    // Validate dates before proceeding
    if (!validateDateRange()) {
        return;
    }

    try {
        // Show loading state
        const generateButton = document.querySelector('.generate-button');
        const originalText = generateButton.textContent;
        generateButton.textContent = 'Generating...';
        generateButton.disabled = true;

        // Refresh the report data
        await refreshReportData();

        // Update UI elements
        document.querySelectorAll('.report-section').forEach(section => {
            section.style.display = 'block';
        });

        // Reset button state
        generateButton.textContent = originalText;
        generateButton.disabled = false;

        // Show success message
        showSuccessMessage('Report generated successfully');

    } catch (error) {
        console.error('Error generating report:', error);
        showErrorMessage('Failed to generate report: ' + error.message);
        
        // Reset button state
        const generateButton = document.querySelector('.generate-button');
        generateButton.textContent = 'Generate Report';
        generateButton.disabled = false;
    }
}

// Helper function to update the report display with the fetched data
function updateReportDisplay(data) {
    console.log('Updating report display with data:', data);

    try {
        // Update Delivery Statistics
        if (data.length > 0) {
            let totalPurolator = 0, totalFedex = 0, totalOneCourier = 0, totalGoBolt = 0;
            
            data.forEach(entry => {
                totalPurolator += entry.purolator || 0;
                totalFedex += entry.fedex || 0;
                totalOneCourier += entry.oneCourier || 0;
                totalGoBolt += entry.goBolt || 0;
            });

            document.getElementById('totalPurolator').textContent = totalPurolator;
            document.getElementById('totalFedex').textContent = totalFedex;
            document.getElementById('totalOneCourier').textContent = totalOneCourier;
            document.getElementById('totalGoBolt').textContent = totalGoBolt;
            document.getElementById('reportTotalDeliveries').textContent = 
                totalPurolator + totalFedex + totalOneCourier + totalGoBolt;
        }

        // Update RX Sales Statistics
        if (data.length > 0) {
            let totalNewRx = 0, totalRefills = 0, totalReAuth = 0, totalHold = 0;
            
            data.forEach(entry => {
                totalNewRx += entry.newRx || 0;
                totalRefills += entry.refill || 0;
                totalReAuth += entry.reAuth || 0;
                totalHold += entry.hold || 0;
            });

            document.getElementById('totalNewRx').textContent = totalNewRx;
            document.getElementById('totalRefills').textContent = totalRefills;
            document.getElementById('totalReAuth').textContent = totalReAuth;
            document.getElementById('totalHold').textContent = totalHold;
            document.getElementById('reportTotalProcessed').textContent = 
                totalNewRx + totalRefills + totalReAuth;
        }

        // Update Profile Statistics
        if (data.length > 0) {
            let totalProfiles = 0;
            let totalActivePercentage = 0;
            
            data.forEach(entry => {
                totalProfiles += entry.profilesEntered || 0;
                totalActivePercentage += entry.activePercentage || 0;
            });

            const avgActivePercentage = (totalActivePercentage / data.length).toFixed(2);
            
            document.getElementById('totalProfiles').textContent = totalProfiles;
            document.getElementById('avgActivePercentage').textContent = `${avgActivePercentage}%`;
        }

        // Make report sections visible
        document.querySelectorAll('.report-section').forEach(section => {
            section.style.display = 'block';
        });

    } catch (error) {
        console.error('Error updating report display:', error);
        showErrorMessage('Error updating report display');
    }
}

function setupDeliveryForm() {
    const deliveryForm = document.getElementById('deliveryForm');
    if (deliveryForm) {
        // Remove any existing event listeners
        const clonedForm = deliveryForm.cloneNode(true);
        deliveryForm.parentNode.replaceChild(clonedForm, deliveryForm);
        
        clonedForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            const submitButton = clonedForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }

            console.log('Submitting delivery form...');

            const dateInput = document.getElementById('date');
            if (!dateInput.value) {
                showMessage('Please select a date', 'error');
                return;
            }

            const formData = {
                date: dateInput.value,
                purolator: parseInt(document.getElementById('purolator').value) || 0,
                fedex: parseInt(document.getElementById('fedex').value) || 0,
                oneCourier: parseInt(document.getElementById('oneCourier').value) || 0,
                goBolt: parseInt(document.getElementById('goBolt').value) || 0
            };

            try {
                const response = await submitForm(formData);
                console.log('Delivery submission response:', response);
                showMessage('Successfully Submitted!');
                
                // Optionally reset the form
                clonedForm.reset();

                if (submitButton) {
                    submitButton.disabled = false;
                }
            } catch (error) {
                console.error('Error submitting delivery data:', error);
                showMessage(error.message, 'error');
            }
        });
    }
}

function setupRxSalesForm() {
    const rxSalesForm = document.getElementById('rxSalesForm');
    if (rxSalesForm) {
        // Remove any existing event listeners
        const clonedForm = rxSalesForm.cloneNode(true);
        rxSalesForm.parentNode.replaceChild(clonedForm, rxSalesForm);
        
        clonedForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            const submitButton = clonedForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }

            console.log('Submitting RX Sales form...');

            const formData = {
                date: document.getElementById('date').value,
                // RX Sales data
                newRx: parseInt(document.getElementById('newRx').value) || 0,
                refill: parseInt(document.getElementById('refill').value) || 0,
                reAuth: parseInt(document.getElementById('reAuth').value) || 0,
                hold: parseInt(document.getElementById('hold').value) || 0,
                // Initialize other category fields to 0
                purolator: 0,
                fedex: 0,
                oneCourier: 0,
                goBolt: 0,
                profilesEntered: 0,
                whoFilledRx: 0,
                activePercentage: 0,
                serviceType: null,
                serviceCost: 0
            };

            console.log('RX Sales form data:', formData);

            try {
                const response = await submitForm(formData);
                console.log('RX Sales submission response:', response);
                showMessage('Successfully Submitted!');
                
                // Calculate and update totals
                const totalFilled = formData.newRx + formData.refill + formData.reAuth;
                const totalEntered = totalFilled + formData.hold;
                
                // Update the display totals
                if (document.getElementById('totalFilled')) {
                    document.getElementById('totalFilled').textContent = totalFilled;
                }
                if (document.getElementById('totalEntered')) {
                    document.getElementById('totalEntered').textContent = totalEntered;
                }
                if (document.getElementById('totalPerHour')) {
                    document.getElementById('totalPerHour').textContent = (totalEntered / 8).toFixed(2);
                }

                // Reset the form after successful submission
                clonedForm.reset();
                
                if (submitButton) {
                    submitButton.disabled = false;
                }

            } catch (error) {
                console.error('Error submitting RX Sales data:', error);
                showMessage('Failed to save RX Sales data: ' + error.message, 'error');
            }
        });
    } else {
        console.error('RX Sales form not found in DOM');
    }
}

function setupProfilesForm() {
    const profilesForm = document.getElementById('profilesForm');
    if (profilesForm) {
        // Remove any existing event listeners
        const clonedForm = profilesForm.cloneNode(true);
        profilesForm.parentNode.replaceChild(clonedForm, profilesForm);
        
        clonedForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            const submitButton = clonedForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }

            console.log('Submitting Weekly Profiles form...');

            // Validate inputs
            const profilesEntered = parseInt(document.getElementById('profilesEntered').value) || 0;
            const whoFilledRx = parseInt(document.getElementById('whoFilledRx').value) || 0;
            let activePercentage = parseFloat(document.getElementById('activePercentage').value) || 0;

            // Calculate active percentage if not manually entered
            if (activePercentage === 0 && profilesEntered > 0) {
                activePercentage = (whoFilledRx / profilesEntered) * 100;
            }

            const formData = {
                date: document.getElementById('date').value,
                // Weekly Profiles data
                profilesEntered: profilesEntered,
                whoFilledRx: whoFilledRx,
                activePercentage: activePercentage,
                // Initialize other fields to 0
                purolator: 0,
                fedex: 0,
                oneCourier: 0,
                goBolt: 0,
                newRx: 0,
                refill: 0,
                reAuth: 0,
                hold: 0,
                serviceType: null,
                serviceCost: 0
            };

            console.log('Weekly Profiles form data:', formData);

            try {
                const response = await submitForm(formData);
                console.log('Weekly Profiles submission response:', response);
                showMessage('Successfully Submitted!');

                // Update weekly summary
                if (document.getElementById('weeklyTotalProfiles')) {
                    document.getElementById('weeklyTotalProfiles').textContent = profilesEntered;
                }
                if (document.getElementById('weeklyAverageActive')) {
                    document.getElementById('weeklyAverageActive').textContent = 
                        `${activePercentage.toFixed(2)}%`;
                }

                // Reset the form after successful submission
                clonedForm.reset();

                if (submitButton) {
                    submitButton.disabled = false;
                }

            } catch (error) {
                console.error('Error submitting Weekly Profiles data:', error);
                showMessage('Failed to save Weekly Profiles data: ' + error.message, 'error');
            }
        });
    } else {
        console.error('Profiles form not found in DOM');
    }
}

function showMessage(message, type = 'success') {
    const bubble = document.createElement('div');
    bubble.className = `message-bubble ${type}-bubble`;
    bubble.textContent = message;

    messageContainer.appendChild(bubble);

    // Remove the message after 3 seconds
    setTimeout(() => {
        bubble.style.animation = 'fadeOut 0.3s ease-out forwards';
        setTimeout(() => {
            messageContainer.removeChild(bubble);
        }, 300);
    }, 3000);
}
