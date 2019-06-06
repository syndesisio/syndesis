import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from 'react-testing-library';
import { ConnectionCard } from '../src/Connection';

export default describe('ConnectionCard', () => {
  const testComponent = (
    <MemoryRouter>
      <ConnectionCard
        name={'Sample connection'}
        configurationRequired={false}
        description={'Sample connection description'}
        icon={<div />}
        i18nCannotDelete={
          'Unable to delete this connection as it is being used by one or more integrations'
        }
        i18nConfigurationRequired={'Configuration Required'}
        i18nTechPreview={'Technology Preview'}
        href={'/test'}
        techPreview={false}
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
