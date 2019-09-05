/**
 * 01: CREATE DATA
 *
 * The purpose of this "test" is to setup data as the user would in the UI.
 * First, the database is reset, and then custom Cypress commands are used
 * to create the data manually.
 */

describe('Create Data', () => {
  before(function() {
    cy.visit('/');
  });

  it('resets the database', () => {
    cy.get('[data-testid=dashboard-page-total-integrations]').should(
      'contain',
      'Integrations'
    );

    expect(cy.get('[data-testid=appTopMenu]')).to.exist;
    expect(cy.get('<header>')).to.exist;
  });
});
