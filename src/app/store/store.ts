import { RouterState } from '@ngrx/router-store';
import { createSelector } from 'reselect';
import { routerReducer } from '@ngrx/router-store';

import { IntegrationEntities } from './integration/integration.model';
import * as fromIntegrations from './integration/integration.reducer';
import { TemplateEntities } from './template/template.model';
import * as fromTemplates from './template/template.reducer';
import { ConnectionEntities } from './connection/connection.model';
import * as fromConnections from './connection/connection.reducer';
/**
 * Treat each reducer like a table in a database. This means
 * our top level state interface is just a map of keys to inner state types.
 */
export interface State {
  integrations: IntegrationEntities;
  templates: TemplateEntities;
  connections: ConnectionEntities;
  router: RouterState;
}

export const reducers = {
  integrations: fromIntegrations.reducer,
  templates: fromTemplates.reducer,
  connections: fromConnections.reducer,
  router: routerReducer,
};

export const getIntegrationsState = (state: State) => state.integrations;

export const getIntegrationEntities = createSelector(getIntegrationsState, fromIntegrations.getEntities);
export const getIntegrationIds = createSelector(getIntegrationsState, fromIntegrations.getIds);
export const getSelectedIntegrationId = createSelector(getIntegrationsState, fromIntegrations.getSelectedId);
export const getSelectedIntegration = createSelector(getIntegrationsState, fromIntegrations.getSelected);
export const getIntegrations = createSelector(getIntegrationEntities, getIntegrationIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});

export const getTemplatesState = (state: State) => state.templates;

export const getTemplateEntities = createSelector(getTemplatesState, fromTemplates.getEntities);
export const getTemplateIds = createSelector(getTemplatesState, fromTemplates.getIds);
export const getSelectedTemplateId = createSelector(getTemplatesState, fromTemplates.getSelectedId);
export const getSelectedTemplate = createSelector(getTemplatesState, fromTemplates.getSelected);
export const getTemplates = createSelector(getTemplateEntities, getTemplateIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});

export const getConnectionsState = (state: State) => state.connections;

export const getConnectionEntities = createSelector(getConnectionsState, fromConnections.getEntities);
export const getConnectionIds = createSelector(getConnectionsState, fromConnections.getIds);
export const getSelectedConnectionId = createSelector(getConnectionsState, fromConnections.getSelectedId);
export const getSelectedConnection = createSelector(getConnectionsState, fromConnections.getSelected);
export const getConnections = createSelector(getConnectionEntities, getConnectionIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});
