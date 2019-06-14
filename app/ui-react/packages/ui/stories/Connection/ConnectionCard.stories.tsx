import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { ConnectionCard, Container } from '../../src';

const stories = storiesOf('Connection/ConnectionCard', module);

const handleDelete = () => {
  // do delete here
};

const menuProps = {
  editHref: '/connections/1234?edit=true',
  i18nCancelLabel: 'Cancel',
  i18nDeleteLabel: 'Delete',
  i18nDeleteModalMessage: 'Are you sure you want to delete this connection',
  i18nDeleteModalTitle: 'Confirm Delete',
  i18nEditLabel: 'Edit',
  i18nMenuTitle: 'Connection Actions',
  i18nViewLabel: 'View',
  isDeleteEnabled: boolean('enableDelete', true),
  onDelete: handleDelete,
};

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
    provide early access to features that are not yet supported. Feedback about
    these features is welcome. Send a message to{' '}
    <a href="mailto:fuse-online-tech-preview@redhat.com">
      fuse-online-tech-preview@redhat.com
    </a>
    .
  </div>
);

stories.add('no menu', () => (
  <MemoryRouter>
    <ConnectionCard
      description={text('description', 'Sample connection description')}
      href={text('#example') || ''}
      i18nConfigRequired={'Configuration Required'}
      i18nTechPreview={'Technology Preview'}
      icon={<div />}
      isConfigRequired={false}
      isTechPreview={false}
      name={text('name', 'Sample connection')}
    />
  </MemoryRouter>
));

stories.add('with menu', () => (
  <MemoryRouter>
    <Container style={{ width: '50%' }}>
      <ConnectionCard
        description={text('description', 'Sample connection description')}
        href={text('#example') || ''}
        i18nCannotDelete={
          'Unable to delete this connection as it is being used by one or more integrations'
        }
        i18nConfigRequired={'Configuration Required'}
        i18nTechPreview={'Technology Preview'}
        icon={<div />}
        isConfigRequired={false}
        menuProps={menuProps}
        name={text('name', 'Sample connection')}
      />
    </Container>
  </MemoryRouter>
));

stories.add('tech preview and configuration required', () => (
  <MemoryRouter>
    <Container style={{ width: '50%' }}>
      <ConnectionCard
        description={text('description', 'Sample connection description')}
        href={text('#example') || ''}
        i18nCannotDelete={
          'Unable to delete this connection as it is being used by one or more integrations'
        }
        i18nConfigRequired={'Configuration Required'}
        i18nTechPreview={'Technology Preview'}
        isConfigRequired={true}
        isTechPreview={true}
        icon={<div />}
        menuProps={menuProps}
        name={text('name', 'Sample connection')}
        techPreviewPopoverHtml={techPreviewPopoverHtml}
      />
    </Container>
  </MemoryRouter>
));
