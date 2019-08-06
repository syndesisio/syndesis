describe('Integration List', () => {
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
});
