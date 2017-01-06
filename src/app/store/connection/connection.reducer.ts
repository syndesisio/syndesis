import { createSelector } from 'reselect';

import { initialEntities } from '../entity/entity.model';
import { Connection, ConnectionEntities } from './connection.model';
import { Actions, ActionTypes } from './connection.actions';

export function reducer(
  state = initialEntities<Connection>(),
  action: Actions,
): ConnectionEntities {
  switch (action.type) {
    case ActionTypes.LOAD_SUCCESS: {
      const connections = action.payload;
      const newConnections = connections.filter(connection => !state.entities[connection.id]);

      const newConnectionIds = newConnections.map(connection => connection.id);
      const newConnectionEntities = newConnections.reduce((entities: { [id: string]: Connection }, connection: Connection) => {
        return Object.assign(entities, {
          [connection.id]: connection,
        });
      }, {});

      return {
        ids: [...state.ids, ...newConnectionIds],
        entities: Object.assign({}, state.entities, newConnectionEntities),
        selectedEntityId: state.selectedEntityId,
      };
    }

    case ActionTypes.LOAD: {
      const connection = action.payload;

      if (state.ids.indexOf(connection.id) > -1) {
        return state;
      }

      return {
        ids: [...state.ids, connection.id],
        entities: Object.assign({}, state.entities, {
          [connection.id]: connection,
        }),
        selectedEntityId: state.selectedEntityId,
      };
    }

    case ActionTypes.SELECT: {
      return {
        ids: state.ids,
        entities: state.entities,
        selectedEntityId: action.payload,
      };
    }

    default: return state;
  }
}

export const getEntities = (state: ConnectionEntities) => state.entities;

export const getIds = (state: ConnectionEntities) => state.ids;

export const getSelectedId = (state: ConnectionEntities) => state.selectedEntityId;

export const getSelected = createSelector(getEntities, getSelectedId, (entities, selectedId) => {
  return entities[selectedId];
});

export const getAll = createSelector(getEntities, getIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});
