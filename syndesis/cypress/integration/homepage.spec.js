describe('The Home Page', () => {
  it('successfully loads', () => {
    cy.visit('/');
    cy.contains('14 Integrations');
    cy.contains('17 Connections');
    cy.contains('0 Messages');
  });
});
