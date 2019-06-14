import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from 'react-testing-library';
import { ConnectionCard } from '../src/Connection';

export default describe('ConnectionCard', () => {
  const testComponent = (
    <MemoryRouter>
      <ConnectionCard
        name={'Sample connection'}
        description={'Sample connection description'}
        icon={<div />}
        i18nCannotDelete={
          'Unable to delete this connection as it is being used by one or more integrations'
        }
        i18nConfigRequired={'Configuration Required'}
        i18nTechPreview={'Technology Preview'}
        isConfigRequired={false}
        isTechPreview={false}
        href={'/test'}
        techPreviewPopoverHtml={
          <span>
            Some popover <strong>HTML</strong>.
          </span>
        }
      />
    </MemoryRouter>
  );

  it('Should have the Sample connection title', () => {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('connection-card-details-link')).toHaveTextContent(
      'Sample connection'
    );
  });
});
