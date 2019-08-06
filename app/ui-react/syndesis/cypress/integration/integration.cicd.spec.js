describe('Integration CI.CD', () => {
  it('loads the integration list from the sidebar', () => {
    cy.visit('/');
    cy.get('[data-testid=ui-integrations]')
      .should('be.visible')
      .click();

    cy.location('pathname').should('contain', '/integrations');
  });

  it('loads the Manage CI/CD page', () => {
    cy.get('[data-testid=integrations-list-view-manage-cicd-button]')
      .should('exist')
      .click();
    cy.location('pathname').should('contain', 'manageCicd');
    cy.get('[data-testid=simple-page-header-title]').should(
      'contain',
      'Manage CI/CD'
    );
  });

  it.skip('creates a new env', () => {
    //cy.get('[data-testid=cicd-list-empty-state-add-new-button]').should('be.visible');
  });

  it.skip('opens the CI/CD dialog', () => {
    // checks if there is an env
  });
  it.skip('adds an environment', () => {});
  it.skip('uploads the imported integration', () => {});
});
