/* tslint:disable:object-literal-sort-keys no-empty-interface */
import {
  QueryResults,
  RestDataService,
  SchemaNodeInfo,
  ViewDefinition,
} from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

// TODO: unit test every single one of these resolvers 😫
export default {
  virtualizations: {
    views: {
      root: makeResolver<{ virtualization: RestDataService }>(
        routes.virtualizations.virtualization.views.root,
        ({ virtualization }) => ({
          params: {
            virtualizationId: virtualization.keng__id,
          },
          state: {
            virtualization,
          },
        })
      ),
      createView: {
        root: makeResolverNoParams(
          routes.virtualizations.virtualization.views.createView.root
        ),
        selectSources: makeResolver<{ virtualization: RestDataService }>(
          routes.virtualizations.virtualization.views.createView.selectSources,
          ({ virtualization }) => ({
            params: {
              virtualizationId: virtualization.keng__id,
            },
            state: {
              virtualization,
            },
          })
        ),
        selectName: makeResolver<{
          schemaNodeInfo: SchemaNodeInfo[];
          virtualization: RestDataService;
        }>(
          routes.virtualizations.virtualization.views.createView.selectName,
          ({ schemaNodeInfo, virtualization }) => ({
            params: {
              virtualizationId: virtualization.keng__id,
            },
            state: {
              schemaNodeInfo,
              virtualization,
            },
          })
        ),
      },
      edit: {
        sql: makeResolver<{
          virtualization: RestDataService;
          viewDefinitionId: string;
          previewExpanded: boolean;
          viewDefinition?: ViewDefinition;
          queryResults: QueryResults;
        }>(
          routes.virtualizations.virtualization.views.edit.sql,
          ({ virtualization, viewDefinitionId, viewDefinition, previewExpanded, queryResults }) => ({
            params: {
              virtualizationId: virtualization.keng__id,
              viewDefinitionId,
            },
            state: {
              virtualization,
              previewExpanded,
              viewDefinition,
              queryResults
            },
          })
        ),
      },
      importSource: {
        root: makeResolverNoParams(
          routes.virtualizations.virtualization.views.importSource.root
        ),
        selectConnection: makeResolver<{ virtualization: RestDataService }>(
          routes.virtualizations.virtualization.views.importSource
            .selectConnection,
          ({ virtualization }) => ({
            params: {
              virtualizationId: virtualization.keng__id,
            },
            state: {
              virtualization,
            },
          })
        ),
        selectViews: makeResolver<{
          connectionId: string;
          virtualization: RestDataService;
        }>(
          routes.virtualizations.virtualization.views.importSource.selectViews,
          ({ connectionId, virtualization }) => ({
            params: {
              virtualizationId: virtualization.keng__id,
            },
            state: {
              connectionId,
              virtualization,
            },
          })
        ),
      },
    },
    relationship: makeResolver<{ virtualization: RestDataService }>(
      routes.virtualizations.virtualization.relationship,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.keng__id,
        },
        state: {
          virtualization,
        },
      })
    ),
    sqlClient: makeResolver<{ virtualization: RestDataService }>(
      routes.virtualizations.virtualization.sqlClient,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.keng__id,
        },
        state: {
          virtualization,
        },
      })
    ),
    metrics: makeResolver<{ virtualization: RestDataService }>(
      routes.virtualizations.virtualization.metrics,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.keng__id,
        },
        state: {
          virtualization,
        },
      })
    ),
    create: makeResolverNoParams(routes.virtualizations.create),
    import: makeResolverNoParams(routes.virtualizations.import),
    list: makeResolverNoParams(routes.virtualizations.list),
  },
  root: makeResolverNoParams(routes.virtualizations.list),
};
