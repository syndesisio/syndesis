import {
  getActionById,
  getConnectionConnector,
  getConnectorActions,
  WithActionDescriptor,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Action, IConnectionOverview } from '@syndesis/models';
import { PageSectionLoader } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ApiError } from '../../../../../shared';
import { ConfigurationForm } from './ConfigurationForm';
import { NothingToConfigure } from './NothingToConfigure';

// Special action IDs
export const API_PROVIDER_END_ACTION_ID = 'io.syndesis:api-provider-end';

export interface IOnUpdatedIntegrationProps {
  /**
   * the action object that has been configured.
   */
  action: Action;
  /**
   * true if the configuration is not complete because there are other steps,
   * false otherwise.
   * If true the form should be re-rendered with an incremented
   * [configurationPage]{@link IWithConfigurationFormProps#configurationPage}.
   */
  moreConfigurationSteps: boolean;
  /**
   * the configured values.
   */
  values: { [key: string]: string } | null;
}

export interface IWithConfigurationFormProps {
  /**
   * the connection object that contains the action with the provided
   * [actionId]{@link IWithConfigurationFormProps#actionId}. Used to retrieve
   * the form definition.
   */
  connection: IConnectionOverview;
  /**
   * the ID of the action that needs to be configured.
   */
  actionId: string;

  oldAction?: Action;
  /**
   * for actions whose configuration must be performed in multiple steps,
   * indicates the current step.
   */
  configurationPage: number;
  /**
   * the values to assign to the form once rendered. These can come either from
   * an existing integration or from the [onUpdatedIntegration]{@link IWithConfigurationFormProps#onUpdatedIntegration}
   * callback.
   */
  initialValue?: { [key: string]: string };

  chooseActionHref: H.LocationDescriptor;

  /**
   * the callback that is fired after the form submit with valid values.
   *
   * @see [action]{@link IOnUpdatedIntegrationProps#action}
   * @see [moreConfigurationSteps]{@link IOnUpdatedIntegrationProps#moreConfigurationSteps}
   * @see [values]{@link IOnUpdatedIntegrationProps#values}
   */
  onUpdatedIntegration(props: IOnUpdatedIntegrationProps): Promise<void>;
}

/**
 * A component to generate a configuration form for a given action and values.
 *
 * @see [action]{@link IWithConfigurationFormProps#action}
 * @see [moreConfigurationSteps]{@link IWithConfigurationFormProps#moreConfigurationSteps}
 * @see [values]{@link IWithConfigurationFormProps#values}
 */
export const WithConfigurationForm: React.FunctionComponent<
  IWithConfigurationFormProps
> = props => {
  const action = getActionById(
    getConnectorActions(getConnectionConnector(props.connection)),
    props.actionId
  );
  // For the API provider end action, the descriptor stored in the integration is the source of truth
  if (props.actionId === API_PROVIDER_END_ACTION_ID) {
    return (
      <ConfigurationForm action={action} descriptor={action.descriptor!} {...props}>
        <NothingToConfigure
          action={action}
          descriptor={action.descriptor!}
          {...props}
        />
      </ConfigurationForm>
    );
  }
  // For all other actions, the descriptor is fetched from the meta service
  return (
    <WithActionDescriptor
      connectionId={props.connection.id!}
      actionId={action.id!}
      initialValue={action.descriptor}
      configuredProperties={props.initialValue || {}}
    >
      {({ data, hasData, error, errorMessage }) => (
        <WithLoader
          error={error}
          loading={!hasData}
          loaderChildren={<PageSectionLoader />}
          errorChildren={<ApiError error={errorMessage!} />}
        >
          {() => (
            <ConfigurationForm action={action} descriptor={data} {...props}>
              <NothingToConfigure
                action={action}
                descriptor={data}
                {...props}
              />
            </ConfigurationForm>
          )}
        </WithLoader>
      )}
    </WithActionDescriptor>
  );
};
