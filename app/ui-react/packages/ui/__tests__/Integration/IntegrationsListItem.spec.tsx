import 'jest-dom/extend-expect';
import * as React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { cleanup, render } from 'react-testing-library';
import { toValidHtmlId } from '../../src';
import {
  IntegrationsListItem,
  IIntegrationsListItemProps,
} from '../../src/Integration';

export default describe('IntegrationsListItem', () => {
  let props = {
    integrationName: 'Sample name',
    startConnectionIcon: <div />,
    finishConnectionIcon: <div />,
    actions: null,
    i18nConfigurationRequired: 'Configuration Required',
    i18nError: 'Error',
    i18nPublished: 'Published',
    i18nProgressPending: 'Pending',
    i18nProgressStarting: 'Starting...',
    i18nProgressStopping: 'Stopping...',
    i18nUnpublished: 'Unpublished',
    i18nLogUrlText: 'View Log',
  } as IIntegrationsListItemProps;

  afterEach(cleanup);

  it('Should render correctly for a running integration', () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem {...props} currentState={'Published'} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);

    expect(
      getByTestId(
        'integrations-list-item-' +
          toValidHtmlId(props.integrationName) +
          '-list-item'
      )
    ).toBeVisible();

    expect(getByTestId('integration-status-status-label')).toBeVisible();
    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Published'
    );
  });

  it('Should render correctly for a pending integration that has started', () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem
          {...props}
          currentState={'Pending'}
          monitoringCurrentStep={2}
          monitoringTotalSteps={3}
          monitoringValue={'Building'}
        />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);

    expect(getByTestId('integration-status-detail')).toBeVisible();
    expect(getByTestId('integration-status-detail')).toHaveTextContent(
      'Building ( 2 / 3 )'
    );
  });

  it("Should render correctly for a pending integration that hasn't started", () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem {...props} currentState={'Pending'} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);

    expect(getByTestId('integration-status-detail')).toHaveTextContent(
      props.i18nProgressPending
    );
  });

  it('Should render correctly for a stopped integration', () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem {...props} currentState={'Unpublished'} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);
    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Unpublished'
    );
  });

  it('Should render correctly for an errored integration', () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem {...props} currentState={'Error'} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);
    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Error'
    );
  });

  it('Should show if configuration is required', () => {
    const testComponent = (
      <MemoryRouter>
        <IntegrationsListItem {...props} isConfigurationRequired={true} />
      </MemoryRouter>
    );

    const { getByTestId } = render(testComponent);
    expect(getByTestId('integrations-list-item-config-required')).toBeVisible();
  });
});
