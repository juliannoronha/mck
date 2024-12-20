/* =============================================================================
 * Wellca Management Module
 * 
 * PURPOSE: Handles tab navigation, form submissions, and dynamic calculations
 * for the Wellca management system
 * ============================================================================= */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize tab functionality
    initializeTabs();
    
    // Initialize date picker with today's date
    document.getElementById('date').valueAsDate = new Date();
    
    // Setup form submissions and calculations
    setupFormHandlers();
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
        servicesForm.addEventListener('submit', handleServiceSubmission);
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
    // Implementation for refreshing report data
    // This will be called when switching to the reports tab
}
