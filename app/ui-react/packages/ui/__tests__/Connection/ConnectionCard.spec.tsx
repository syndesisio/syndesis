import * as React from 'react';
import 'jest-dom/extend-expect';
import { MemoryRouter } from 'react-router';
import { cleanup, fireEvent, render } from 'react-testing-library';
import { ConnectionCard } from '../../src/Connection';

export default describe('ConnectionCard', () => {
  const techPreviewPopoverHtml = (
    <div>
      <a
        href="https://access.redhat.com/support/offerings/techpreview"
        rel="nofollow"
        target="_blank"
        role="link"
      >
        Technology Previews
      </a>{' '}
      provide early access to features that are not yet supported. Feedback
      about these features is welcome. Send a message to{' '}
      <a href="mailto:fuse-online-tech-preview@redhat.com">
        fuse-online-tech-preview@redhat.com
      </a>
      .
    </div>
  );

  const props = {
    name: 'Sample Connection',
    description: 'Sample connection description',
    href: '/test',
    icon: <div />,
    i18nCannotDelete:
      'Unable to delete this connection as it is being used by one or more integrations',
    i18nConfigRequired: 'Configuration Required',
    i18nTechPreview: 'Technology Preview',
    isConfigRequired: false,
    isTechPreview: false,
    techPreviewPopoverHtml: techPreviewPopoverHtml,
  };

  const testComponent = (
    <MemoryRouter>
      <ConnectionCard {...props} />
    </MemoryRouter>
  );

  const handleDelete = jest.fn();

  const menuProps = {
    editHref: '/connections/1234?edit=true',
    i18nCancelLabel: 'Cancel',
    i18nDeleteLabel: 'Delete',
    i18nDeleteModalMessage: 'Are you sure you want to delete this connection',
    i18nDeleteModalTitle: 'Confirm Delete',
    i18nEditLabel: 'Edit',
    i18nMenuTitle: 'Connection Actions',
    i18nViewLabel: 'View',
    isDeleteEnabled: true,
    onDelete: handleDelete,
  };

  afterEach(cleanup);

  it('should have the Sample connection title', () => {
    const { getByTestId } = render(testComponent);

    expect(getByTestId('connection-card-details-link')).toHaveTextContent(
      props.name
    );
    expect(getByTestId('connection-card-description')).toHaveTextContent(
      props.description
    );
  });

  it('should render with a dropdown menu', () => {
    const testComponentWithMenu = (
      <MemoryRouter>
        <ConnectionCard {...props} menuProps={menuProps} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponentWithMenu);

    expect(getByTestId('connection-card-dropdown')).toBeVisible();
    fireEvent.click(getByTestId('connection-card-kebab'));
    expect(getByTestId('connection-card-view-action')).toBeVisible();
    expect(getByTestId('connection-card-edit-action')).toBeVisible();
    expect(getByTestId('connection-card-delete-action')).toBeVisible();
  });

  it('should show a tech preview and configuration required banner', () => {
    const testComponentConfigTechPreview = (
      <MemoryRouter>
        <ConnectionCard
          {...props}
          isConfigRequired={true}
          isTechPreview={true}
        />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponentConfigTechPreview);

    expect(getByTestId('connection-card-config-required-footer')).toBeVisible();
    expect(getByTestId('connection-card-tech-preview-heading')).toBeVisible();
  });
});
