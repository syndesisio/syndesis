import { IpaasClientCliPage } from './app.po';

describe('ipaas-ui App', function() {
  let page: IpaasClientCliPage;

  beforeEach(() => {
    page = new IpaasClientCliPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
