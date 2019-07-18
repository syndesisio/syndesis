describe('Create a Connection', () => {
  beforeEach(function() {
    cy.visit('/');
  });

  /**
   * Happy Path
   * 1.
   */

  it('loads the Create Connection page successfully', () => {
    cy.get('[data-testid=dashboard-create-connection-button]').click();
    cy.location('pathname').should(
      'contain',
      '/connections/create/connection-basics'
    );

    cy.get('.pf-c-page__sidebar-body').should('be.hidden');
  });

  /**
   * Alternative Path
   */

  it.skip('', () => {});

  /**
   * Exception Path
   */

  it.skip('', () => {});
});
