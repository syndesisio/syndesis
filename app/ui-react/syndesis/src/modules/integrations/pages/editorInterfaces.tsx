import { ConnectionOverview, Integration } from '@syndesis/models';

/**
 * @param actionId - the ID of the action selected in the previous step.
 * @param position - the zero-based position for the new step in the integration
 * flow.
 * @param step - the configuration step when configuring a multi-page connection.
 */
export interface IConfigureActionRouteParams {
  flow: string;
  position: string;
  actionId: string;
  step?: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step. Needed
 * to render the IVP.
 * @param updatedIntegration - when creating a link to this page, this should
 * never be set. It is used by the page itself to pass the partially configured
 * step when configuring a multi-page connection.
 */
export interface IConfigureActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
  updatedIntegration?: Integration;
  configuredProperties: { [key: string]: string };
}

/**
 * @param connectionId - the ID of the connection selected in the previous step
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectActionRouteParams {
  flow: string;
  connectionId: string;
  position: string;
}

/**
 * @param integration - the integration object, used to render the IVP.
 * @param connection - the connection object selected in the previous step, used
 * to render the IVP.
 */
export interface ISelectActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
}

/**
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectConnectionRouteParams {
  flow: string;
  position: string;
}

/**
 * @param integration - the integration object coming from step 3.index, used to
 * render the IVP.
 */
export interface ISelectConnectionRouteState {
  integration: Integration;
}

export interface IBaseRouteParams {
  flow: string;
}

export interface IBaseRouteState {
  /**
   * the integration object to edit
   */
  integration: Integration;
}

/**
 * @param integration - the integration object.
 */
export interface ISaveIntegrationRouteParams {
  flow: string;
}

export interface ISaveIntegrationForm {
  name: string;
  description?: string;
}

/**
 * @param integration - the integration object.
 */
export interface ISaveIntegrationRouteState {
  integration: Integration;
}
