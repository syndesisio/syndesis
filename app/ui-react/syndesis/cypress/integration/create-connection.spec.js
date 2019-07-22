describe('Create a Connection', () => {
  /**
   * Happy Path
   * 1. User navigates to Create Connection page from the Dashboard.
   * 2. User is able to see a list of Connectors.
   * 3. User selects a Connector, and is redirected to Step 2.
   * 4. User is able to populate configuration fields in Step 2, with validation.
   * 5. User selects Next button to move to Step 3.
   * 6. User is able to add a name and description in Step 3, with validation.
   * 7. User is able to click Save to create Connection.
   * 8. User is redirected to Connections list page and sees newly created Connection.
   */

  it('loads the Create Connection page and Step 1: Select connector page', () => {
    cy.visit('/');

    cy.get('[data-testid=dashboard-create-connection-button]').click();

    cy.location('pathname').should(
      'contain',
      '/connections/create/connection-basics'
    );

    cy.get('.pf-c-page__sidebar-body').should('be.hidden');

    cy.get('.active > a > .wizard-pf-step-number').should('have.text', '1');
  });

  it('loads Step 2: Configure connection page with DB connector', () => {
    /**
     * DB connector
     */
    cy.get('[data-testid=connection-card-database-card]')
      .should('exist')
      .click();

    cy.location('pathname').should(
      'contain',
      '/connections/create/sql/configure-fields'
    );

    cy.get('.active > a > .wizard-pf-step-number').should('have.text', '2');

    cy.get('[data-testid=connection-creator-layout-next-button]').should(
      'be.disabled'
    );

    cy.get('[data-testid=url]').focus();

    cy.get('[data-testid=user]').focus();

    cy.get('.form-group.has-error').should('be.visible');

    cy.get('[data-testid=url]')
      .type('jdbc:postgresql://syndesis-db:5432/sampledb')
      .should('have.value', 'jdbc:postgresql://syndesis-db:5432/sampledb');

    cy.get('[data-testid=user]')
      .type('sampledb')
      .should('have.value', 'sampledb');

    cy.get('[data-testid=password]')
      .type('sampledb')
      .should('have.value', 'sampledb');

    cy.get('[data-testid=schema]')
      .type('sampledb')
      .should('have.value', 'sampledb');

    cy.get('[data-testid=connection-creator-layout-next-button]')
      .should('not.be.disabled')
      .click();
  });

  it('loads Step 3: Name connection', () => {
    cy.get('.active > a > .wizard-pf-step-number').should('have.text', '3');

    cy.get('[data-testid=name]')
      .type('Test PostgreSQL DB')
      .should('have.value', 'Test PostgreSQL DB');

    cy.get('[data-testid=description]')
      .type('Subscribe for and publish messages.')
      .should('have.value', 'Subscribe for and publish messages.');

    cy.get('[data-testid=connection-creator-layout-next-button]').should(
      'not.be.disabled'
    );
  });

  /**
   * Alternative Path
   */

  it.skip('', () => {
    cy.visit('/');
  });

  /**
   * Exception Path
   */

  it.skip('', () => {
    cy.visit('/');
  });
});
