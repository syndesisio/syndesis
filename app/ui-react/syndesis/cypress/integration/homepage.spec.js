describe('The Home Page', () => {
  it('successfully loads', () => {
    cy.visit('/');

    cy.get('[data-testid=total-integrations]').should(
      'contain',
      '16 Integrations'
    );

    cy.get('[data-testid=total-connections]').should(
      'contain',
      '18 Connections'
    );

    cy.get('[data-testid=total-messages]').should('contain', '0 Messages');
  });
});
