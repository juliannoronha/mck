/* =============================================================================
 * Wellca Management Module
 * 
 * PURPOSE: Handles tab navigation, form submissions, and dynamic calculations
 * for the Wellca management system
 * ============================================================================= */

let messageContainer;

document.addEventListener('DOMContentLoaded', function() {
    setupDeliveryForm();
    setupRxSalesForm();
    setupServicesForm();
    initializeTabs();
    setupFormHandlers();
    document.getElementById('date').valueAsDate = new Date();

    if (!messageContainer) {
        messageContainer = document.createElement('div');
        messageContainer.className = 'message-container';
        document.body.appendChild(messageContainer);
    }
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
        // Remove any existing event listeners
        const clonedForm = servicesForm.cloneNode(true);
        servicesForm.parentNode.replaceChild(clonedForm, servicesForm);
        
        clonedForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            const submitButton = clonedForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
            }

            console.log('Submitting Professional Services form...');

            try {
                // Get form values with enhanced validation
                const serviceType = document.getElementById('serviceType').value;
                const serviceCost = parseFloat(document.getElementById('serviceCost').value) || 0;

                console.log('Validating Professional Services input:', {
                    serviceType,
                    serviceCost
                });

                if (!serviceType) {
                    console.error('Service type validation failed: empty value');
                    showMessage('Please enter a service type', 'error');
                    return;
                }

                const formData = {
                    date: document.getElementById('date').value,
                    serviceType: serviceType,
                    serviceCost: serviceCost,
                    // Delivery fields
                    purolator: 0,
                    fedex: 0,
                    oneCourier: 0,
                    goBolt: 0,
                    // RX Sales fields
                    newRx: 0,
                    refill: 0,
                    reAuth: 0,
                    hold: 0,
                    // Profile fields (required by backend)
                    profilesEntered: 0,
                    whoFilledRx: 0,
                    activePercentage: 0
                };

                console.log('Professional Services form data:', JSON.stringify(formData, null, 2));

                const response = await submitForm(formData);
                console.log('Professional Services submission response:', response);

                if (!response || !response.id) {
                    throw new Error('Invalid response from server');
                }

                showMessage('Successfully Submitted!');

                // Reset the form after successful submission
                clonedForm.reset();

                if (submitButton) {
                    submitButton.disabled = false;
                }

            } catch (error) {
                console.error('Error submitting Professional Services data:', error);
                showMessage('Failed to save Professional Services data: ' + error.message, 'error');
                
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
        console.log('Submitting form data:', JSON.stringify(formData, null, 2));
        
        // Ensure serviceCost is a number
        if (formData.serviceCost) {
            formData.serviceCost = Number(formData.serviceCost);
        }

        // Ensure all number fields are actually numbers
        const numberFields = [
            'purolator', 'fedex', 'oneCourier', 'goBolt', 
            'newRx', 'refill', 'reAuth', 'hold',
            'profilesEntered', 'whoFilledRx', 'activePercentage'
        ];
        
        numberFields.forEach(field => {
            formData[field] = Number(formData[field]) || 0;
        });

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
            const errorText = await response.text();
            console.error('Server response:', errorText);
            try {
                const errorData = JSON.parse(errorText);
                throw new Error(`Submission failed: ${JSON.stringify(errorData)}`);
            } catch (e) {
                throw new Error(`Submission failed: ${errorText}`);
            }
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

            const deliveryElements = {
                'totalPurolator': totalPurolator,
                'totalFedex': totalFedex,
                'totalOneCourier': totalOneCourier,
                'totalGoBolt': totalGoBolt,
                'reportTotalDeliveries': totalPurolator + totalFedex + totalOneCourier + totalGoBolt
            };

            Object.entries(deliveryElements).forEach(([id, value]) => {
                const element = document.getElementById(id);
                if (element) {
                    element.textContent = value;
                }
            });
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

            const rxElements = {
                'totalNewRx': totalNewRx,
                'totalRefills': totalRefills,
                'totalReAuth': totalReAuth,
                'totalHold': totalHold,
                'reportTotalProcessed': totalNewRx + totalRefills + totalReAuth
            };

            Object.entries(rxElements).forEach(([id, value]) => {
                const element = document.getElementById(id);
                if (element) {
                    element.textContent = value;
                }
            });
        }

        // Update Professional Services Summary
        if (data.length > 0) {
            console.log('Processing Professional Services data...');
            
            // Group services by month
            const monthlyServices = new Map();
            let grandTotal = 0;

            data.forEach(entry => {
                if (entry.serviceType && entry.serviceCost !== null) {
                    const date = new Date(entry.date);
                    const monthKey = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`;
                    
                    if (!monthlyServices.has(monthKey)) {
                        monthlyServices.set(monthKey, {
                            total: 0,
                            count: 0,
                            services: new Map()
                        });
                    }

                    const monthData = monthlyServices.get(monthKey);
                    const cost = parseFloat(entry.serviceCost);
                    
                    if (!isNaN(cost)) {
                        monthData.total += cost;
                        monthData.count++;
                        grandTotal += cost;

                        // Count service types
                        const serviceCount = monthData.services.get(entry.serviceType) || 0;
                        monthData.services.set(entry.serviceType, serviceCount + 1);
                    }
                }
            });

            console.log('Monthly services summary:', {
                monthlyData: Object.fromEntries(monthlyServices),
                grandTotal
            });

            // Update the display
            const totalServicesElement = document.getElementById('totalServices');
            const serviceBreakdownElement = document.getElementById('serviceBreakdown');

            if (totalServicesElement && serviceBreakdownElement) {
                totalServicesElement.textContent = grandTotal.toFixed(2);

                let breakdownHtml = '';
                if (monthlyServices.size > 0) {
                    breakdownHtml = '<div class="monthly-breakdown">';
                    
                    // Sort months in descending order
                    const sortedMonths = Array.from(monthlyServices.keys()).sort().reverse();
                    
                    sortedMonths.forEach(month => {
                        const monthData = monthlyServices.get(month);
                        breakdownHtml += `
                            <div class="month-section">
                                <h5>${formatMonthYear(month)}</h5>
                                <div class="month-stats">
                                    <div>Total Revenue: $${monthData.total.toFixed(2)}</div>
                                    <div>Total Services: ${monthData.count}</div>
                                </div>
                                <div class="service-types">
                                    ${Array.from(monthData.services.entries())
                                        .map(([type, count]) => `
                                            <div class="service-type">
                                                ${formatServiceType(type)}: ${count}
                                            </div>
                                        `).join('')}
                                </div>
                            </div>
                        `;
                    });
                    
                    breakdownHtml += `
                        <div class="grand-total">
                            <h5>Grand Total: $${grandTotal.toFixed(2)}</h5>
                        </div>
                    </div>`;
                } else {
                    breakdownHtml = '<div>No services recorded</div>';
                }
                
                serviceBreakdownElement.innerHTML = breakdownHtml;
                console.log('Updated service breakdown with monthly totals');
            }
        }

        // Make report sections visible
        document.querySelectorAll('.report-section').forEach(section => {
            section.style.display = 'block';
        });

    } catch (error) {
        console.error('Error updating report display:', error);
        showMessage('Error updating report display: ' + error.message, 'error');
    }
}

// Helper function to format month-year
function formatMonthYear(monthKey) {
    const [year, month] = monthKey.split('-');
    const date = new Date(year, parseInt(month) - 1);
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
}

// Helper function to format service type
function formatServiceType(type) {
    return type.split('_').map(word => 
        word.charAt(0) + word.slice(1).toLowerCase()
    ).join(' ');
}

function setupDeliveryForm() {
    const deliveryForm = document.getElementById('deliveryForm');
    if (deliveryForm) {
        deliveryForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const submitButton = e.target.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.classList.add('loading');
            }

            try {
                console.log('Submitting delivery form...');

                const formData = {
                    date: document.getElementById('date').value,
                    // Delivery data
                    purolator: parseInt(document.getElementById('purolator').value) || 0,
                    fedex: parseInt(document.getElementById('fedex').value) || 0,
                    oneCourier: parseInt(document.getElementById('oneCourier').value) || 0,
                    goBolt: parseInt(document.getElementById('goBolt').value) || 0,
                    // Initialize other fields to 0 or null
                    newRx: 0,
                    refill: 0,
                    reAuth: 0,
                    hold: 0,
                    profilesEntered: 0,
                    whoFilledRx: 0,
                    activePercentage: 0,
                    serviceType: null,
                    serviceCost: 0
                };

                const response = await submitForm(formData);
                console.log('Delivery submission response:', response);
                showMessage('Successfully Submitted!');

                // Reset the form after successful submission
                deliveryForm.reset();

                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.classList.remove('loading');
                }
            } catch (error) {
                console.error('Error submitting delivery data:', error);
                showMessage('Failed to save delivery data: ' + error.message, 'error');
                
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.classList.remove('loading');
                }
            }
        });
    } else {
        console.error('Delivery form not found in DOM');
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
    const messageContainer = document.querySelector('.message-container');
    const messageElement = document.createElement('div');
    messageElement.className = `message-bubble ${type}-bubble`;
    messageElement.textContent = message;

    // Add animation class
    messageElement.classList.add('slide-in-fade');
    
    messageContainer.appendChild(messageElement);

    // Remove the message after animation completes
    setTimeout(() => {
        messageElement.classList.add('fade-out');
        setTimeout(() => {
            messageContainer.removeChild(messageElement);
        }, 300); // Match the fade-out animation duration
    }, 3000);
}
