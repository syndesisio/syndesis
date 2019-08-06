describe('Integration Import', () => {
  it('loads the integration list from the sidebar', () => {
    cy.visit('/');
    cy.get('[data-testid=ui-integrations]')
      .should('be.visible')
      .click();

    cy.location('pathname').should('contain', '/integrations');

    cy.get('[data-testid=integrations-list-view-import-button]')
      .should('exist')
      .click();
  });

  it('ensures the page renders properly', () => {
    cy.location('pathname').should('contain', 'integrations/import');
    cy.get('[data-testid=import-page]').should('be.visible');
    cy.get('.dnd-file-chooser').should('be.visible');
    cy.get('.dnd-file-chooser').should('contain', 'No file selected');
  });

  it('allows users to select an integration to import', () => {});
  it('uploads the imported integration', () => {});
});
