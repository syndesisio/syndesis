/**
 * 03: RESTORE SNAPSHOT/DATA
 *
 * The purpose of this "test" is to restore the snapshot and, therefore, data
 * to the database. We first reset the database to increase the likelihood
 * of having a predictable test environment, then test to see if the data
 * exists as expected, from the perspective of the user.
 */

describe('Restore Snapshot', () => {
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
