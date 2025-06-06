**Project: Simple Estate/Building Management Application (Developing Countries)**

**Overall Goal:** Develop a simple, user-friendly Android application for managing properties in developing countries, accessible to users with varying levels of literacy and digital experience.

**Guiding Principles:** Simplicity, User-Friendliness, Accessibility, Resource Efficiency.

**Milestone 1: Core Infrastructure and Tenant Management [ ]**

*   [ ] Set up Android project with target Android version 5.0+
*   [ ] Design and implement basic UI framework (navigation, main screens)
*   [ ] Set up SQLite database with basic tenant table (name, apartment number, phone number, other contact info)
*   [ ] Implement UI for adding a new tenant
*   [ ] Implement UI for viewing/searching tenant details
*   [ ] Implement basic data validation (required fields, phone number format)
*   [ ] Initial testing and debugging of tenant management functionality.

**Milestone 2: Invoice Management and Payment Tracking [ ]**

*   [ ] Design and implement invoice table in SQLite database (tenant ID, invoice date, due date, amount due, status (unpaid, partially paid, fully paid))
*   [ ] Implement automatic invoice generation based on rental agreement terms
*   [ ] Design and implement payment table in SQLite database (tenant ID, payment date, payment method, amount paid)
*   [ ] Implement UI for recording rent payments (including fields for payment frequency, method, amount, date)
*   [ ] Implement logic for associating a payment with one or more invoices
*   [ ] Implement logic for handling partial payments and tracking remaining balances on invoices
*   [ ] Implement UI to display invoice payment history for each tenant
*   [ ] Testing and debugging of invoice and payment tracking functionality.

**Milestone 3: Receipt Generation and Sharing [ ]**

*   [ ] Implement PDF receipt generation
    *   [ ] Receipt includes tenant name, apartment number, payment date, total amount paid, invoice breakdown (invoice number/date, original amount, amount paid, remaining balance) and property address.
*   [ ] Implement sharing options for receipts
    *   [ ] Email sharing (if email address is available)
    *   [ ] WhatsApp sharing (if WhatsApp is detected - see Milestone 7)
    *   [ ] Local PDF save
*   [ ] Testing and debugging of receipt generation and sharing.

**Milestone 4: Default Management and Reminders [ ]**

*   [ ] Implement logic for detecting late payments based on invoice due dates
*   [ ] Design and implement UI for configuring reminder settings (reminder timing, message content)
*   [ ] Implement reminder delivery system
    *   [ ] SMS reminders (if feasible and cost-effective, prioritize this after core functions work)
    *   [ ] WhatsApp reminders (if WhatsApp is detected - see Milestone 7)
    *   [ ] In-app notifications (if user has app installed)
*   [ ] Implement default reporting (list of tenants in default, overdue amount, days late)
*   [ ] Testing and debugging of default management and reminder functionality.

**Milestone 5: Maintenance Request Management [ ]**

*   [ ] Design and implement maintenance request table in SQLite database (tenant ID, request date, category, description, status (pending, in progress, completed))
*   [ ] Implement UI for submitting maintenance requests (text description, optional picture upload)
*   [ ] Implement UI for admin to view/manage maintenance requests (change status, add notes)
*   [ ] Testing and debugging of maintenance request management.

**Milestone 6: Reporting and Financials [ ]**

*   [ ] Implement rent collection status report
*   [ ] Implement maintenance request summary report
*   [ ] Implement occupancy rate calculation
*   [ ] Implement UI for creating projected budget (income/expenses)
*   [ ] Implement profit & loss statement generation
*   [ ] Implement prominent tax disclaimer on profit & loss statement: "This report provides general financial information only. It is not intended as professional tax advice. Consult with a qualified tax advisor for accurate tax preparation and filing."
*   [ ] Testing and debugging of reporting and financial functionality.

**Milestone 7: App Detection and Accessibility [ ]**

*   [ ] Implement WhatsApp detection functionality
    *   [ ] "Share via WhatsApp" option is displayed only if WhatsApp is installed
*   [ ] Implement VERY LARGE, CLEAR ICONS and TEXT
*   [ ] Minimize text input (use dropdowns, radio buttons where possible)
*   [ ] Implement voice input (optional, but highly recommended)
*   [ ] Implement offline functionality (prioritize critical data for offline access)
*   [ ] Implement clear visual feedback for user actions
*   [ ] Implement simple onboarding/tutorial process
*   [ ] Final testing of accessibility and user experience.

**Constraints:**

*   [ ] Keep it simple!
*   [ ] Optimize for resource efficiency (low-end devices)
*   [ ] Minimize data usage
*   [ ] Prioritize a quick development cycle within the constraints of functional correctness.

**Overall Testing and Finalization [ ]**

*   [ ] Thorough testing on target devices (older smartphones)
*   [ ] User feedback collection and implementation
*   [ ] Code cleanup and documentation
*   [ ] Final build and deployment.
