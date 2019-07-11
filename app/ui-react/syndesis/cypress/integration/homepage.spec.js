describe('The Home Page', () => {
  it('loads metrics cards successfully', () => {
    cy.visit('/');

    /**
     * We won't be counting the exact number for now, until we get data stubs or fixtures set up.
     */
    cy.get('[data-testid=dashboard-page-total-integrations]').should(
      'contain',
      'Integrations'
    );

    cy.get('[data-testid=dashboard-page-total-connections]').should(
      'contain',
      'Connections'
    );

    cy.get('[data-testid=dashboard-page-total-messages]').should(
      'contain',
      'Messages'
    );

    cy.get('[data-testid=dashboard-page-metrics-uptime]').should(
      'contain',
      'Uptime'
    );
  });

  it('loads top 5 integrations', () => {});
  it('loads integration board', () => {});
  it('loads recent updates', () => {});
  it('loads connections', () => {});
  it('', () => {});
});
