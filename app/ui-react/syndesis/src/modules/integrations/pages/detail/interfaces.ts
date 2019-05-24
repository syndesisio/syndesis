import { IIntegrationOverviewWithDraft } from '@syndesis/models';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IDetailsRouteParams {
  integrationId: string;
}

export interface IDetailsRouteState {
  integration?: IIntegrationOverviewWithDraft;
}
