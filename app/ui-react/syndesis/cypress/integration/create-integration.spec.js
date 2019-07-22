describe('Create an Integration', () => {
  /**
   * Happy Path
   *
   * 1. User navigates to Create Integration page from the Dashboard.
   * 2. User is able to see a list of Connections.
   * 3. User selects a Connection, and is redirected to Choose an Action.
   * 4. User selects one of the connection actions available and is redirected to the next step.
   * 5. User configures the action selected, with validation.
   * 6. User is redirected to select an end Connection.
   * 7. User selects an action for the end Connection.
   * 8. User configures the action selected, with validation.
   * 8. User is given the option of adding a step in between start and end Connections, or to Save or Publish.
   * 9. User is able to name the Integration and then click Save and publish to create Integration.
   * 10. User is redirected to Integrations list page and sees newly created Integration.
   */

  it('loads the editor and prompts users to select a start connection', () => {
    cy.visit('/');

    cy.get('[data-testid=dashboard-create-integration-button]').click();

    cy.location('pathname').should(
      'contain',
      '/integrations/create/new-integration/start/'
    );

    /**
     * Select Timer connection as starting
     */
    cy.get('[data-testid=connection-card-timer-card]').click();
  });

  it('prompts users to select an action for the start connection', () => {
    cy.location('pathname').should('contain', '/connection/timer');

    /**
     * Select the Simple Timer
     */
    cy.get(
      '[data-testid=integration-editor-actions-list-item-simple-list-item]'
    ).within(() => {
      cy.get('[data-testid=select-action-page-select-button]').click();
    });
  });

  it('loads Configure Action for the start connection', () => {
    cy.get('[data-testid=period]')
      .clear()
      .type('100');
    cy.get('#integration-editor-form-next-button')
      .should('not.be.disabled')
      .click();
  });

  it.skip('prompts users to select an end connection', () => {});

  it.skip('prompts users to select an action for the end connection', () => {});

  it.skip('loads Configure Action for the end connection', () => {});

  it.skip('prompts users to add a step', () => {});

  it.skip('allows users to set a name and description', () => {});

  it.skip('loads integration list with newly created integration', () => {
    cy.get('[data-testid=integration-status-detail]').should('be.visible');
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
