describe('Integration List', () => {
  const integrationSlug = 'e2e-todo-integration';

  it('loads the integration list from the sidebar', () => {
    cy.visit('/');
    cy.get('[data-testid=ui-integrations]')
      .should('be.visible')
      .click();

    cy.location('pathname').should('contain', '/integrations');
  });

  it('ensures the page renders properly', () => {
    /**
     * Sidebar should be expanded
     */
    cy.get('.pf-c-page__sidebar-body').should('be.visible');
  });

  /**
   * Skipping this test for now until we get the DB snapshots ready
   */
  it.skip('tests the actions of an integration', () => {
    cy.wait(200);
    /**
     * Start an integration
     */
    cy.get('[data-testid|=integrations-list-item-' + integrationSlug + ']')
      .first()
      .within(() => {
        cy.get('.dropdown-toggle').click();
        cy.get('[data-testid=integration-actions-stop]').click();
      });

    cy.get('.modal-footer > .btn-primary').click();
    /**
     * May not be a good test, re-assess as very dependent on time.
     */
    cy.get('.toast-pf.alert-info')
      .contains('Stopping integration')
      .should('be.visible');

    /**
     * Stop an integration
     */
    cy.get('[data-testid|=integrations-list-item-' + integrationSlug + ']')
      .first()
      .within(() => {
        cy.get('.dropdown-toggle').click();
        cy.get('[data-testid=integration-actions-stop]').click();
      });
    cy.get('.modal-footer > .btn-primary').click();

    cy.get('.toast-pf.alert-info')
      .contains('Starting integration')
      .should('be.visible');

    cy.wait(20000);

    cy.get('[data-testid=progress-with-link-value]').should('be.visible');
  });
});
