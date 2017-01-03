import { createSelector } from 'reselect';

import { initialEntities } from '../entity/entity.model';
import { Integration, IntegrationEntities } from './integration.model';
import { Actions, ActionTypes } from './integration.actions';

export function reducer(
  state = initialEntities<Integration>(),
  action: Actions,
): IntegrationEntities {
  switch (action.type) {
    case ActionTypes.LOAD_SUCCESS: {
      const integrations = action.payload;
      const newIntegrations = integrations.filter(integration => !state.entities[integration.id]);

      const newIntegrationIds = newIntegrations.map(integration => integration.id);
      const newIntegrationEntities = newIntegrations.reduce((entities: { [id: string]: Integration }, integration: Integration) => {
        return Object.assign(entities, {
          [integration.id]: integration,
        });
      }, {});

      return {
        ids: [...state.ids, ...newIntegrationIds],
        entities: Object.assign({}, state.entities, newIntegrationEntities),
        selectedEntityId: state.selectedEntityId,
      };
    }

    case ActionTypes.LOAD: {
      const integration = action.payload;

      if (state.ids.indexOf(integration.id) > -1) {
        return state;
      }

      return {
        ids: [...state.ids, integration.id],
        entities: Object.assign({}, state.entities, {
          [integration.id]: integration,
        }),
        selectedEntityId: state.selectedEntityId,
      };
    }

    default: return state;
  }
}

export const getEntities = (state: IntegrationEntities) => state.entities;

export const getIds = (state: IntegrationEntities) => state.ids;

export const getSelectedId = (state: IntegrationEntities) => state.selectedEntityId;

export const getSelected = createSelector(getEntities, getSelectedId, (entities, selectedId) => {
  return entities[selectedId];
});

export const getAll = createSelector(getEntities, getIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});
