/**
 * 02: CREATE SNAPSHOT
 *
 * The purpose of this "test" is to create a snapshot of the database.
 * This test was separated from the one used to create the data for
 * added flexibility.
 */

describe('Create Snapshot', () => {
  beforeEach(function() {
    cy.visit('/');
  });

  it('loads the dashboard successfully', () => {
    cy.get('[data-testid=dashboard-page-total-integrations]').should(
      'contain',
      'Integrations'
    );

    expect(cy.get('<header>')).to.exist;
  });
});
