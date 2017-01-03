import { createSelector } from 'reselect';

import { initialEntities } from '../entity/entity.model';
import { Template, TemplateEntities } from './template.model';
import { Actions, ActionTypes } from './template.actions';

export function reducer(
  state = initialEntities<Template>(),
  action: Actions,
): TemplateEntities {
  switch (action.type) {
    case ActionTypes.LOAD_SUCCESS: {
      const templates = action.payload;
      const newTemplates = templates.filter(template => !state.entities[template.id]);

      const newTemplateIds = newTemplates.map(integration => integration.id);
      const newTemplateEntities = newTemplates.reduce((entities: { [id: string]: Template }, template: Template) => {
        return Object.assign(entities, {
          [template.id]: template,
        });
      }, {});

      return {
        ids: [...state.ids, ...newTemplateIds],
        entities: Object.assign({}, state.entities, newTemplateEntities),
        selectedEntityId: state.selectedEntityId,
      };
    }

    case ActionTypes.LOAD: {
      const template = action.payload;

      if (state.ids.indexOf(template.id) > -1) {
        return state;
      }

      return {
        ids: [...state.ids, template.id],
        entities: Object.assign({}, state.entities, {
          [template.id]: template,
        }),
        selectedEntityId: state.selectedEntityId,
      };
    }

    default: return state;
  }
}

export const getEntities = (state: TemplateEntities) => state.entities;

export const getIds = (state: TemplateEntities) => state.ids;

export const getSelectedId = (state: TemplateEntities) => state.selectedEntityId;

export const getSelected = createSelector(getEntities, getSelectedId, (entities, selectedId) => {
  return entities[selectedId];
});

export const getAll = createSelector(getEntities, getIds, (entities, ids) => {
  return ids.map(id => entities[id]);
});
