/* tslint:disable:object-literal-sort-keys no-empty-interface */
import {
  SchemaNodeInfo,
  ViewDefinition,
  Virtualization,
} from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

// TODO: unit test every single one of these resolvers 😫
export default {
  virtualizations: {
    views: {
      root: makeResolver<{ virtualization: Virtualization }>(
        routes.virtualizations.virtualization.views.root,
        ({ virtualization }) => ({
          params: {
            virtualizationId: virtualization.name,
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
        selectSources: makeResolver<{ virtualization: Virtualization }>(
          routes.virtualizations.virtualization.views.createView.selectSources,
          ({ virtualization }) => ({
            params: {
              virtualizationId: virtualization.name,
            },
            state: {
              virtualization,
            },
          })
        ),
        selectName: makeResolver<{
          schemaNodeInfo: SchemaNodeInfo[];
          virtualization: Virtualization;
        }>(
          routes.virtualizations.virtualization.views.createView.selectName,
          ({ schemaNodeInfo, virtualization }) => ({
            params: {
              virtualizationId: virtualization.name,
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
          virtualization: Virtualization;
          viewDefinitionId: string;
          viewDefinition?: ViewDefinition;
        }>(
          routes.virtualizations.virtualization.views.edit.sql,
          ({ virtualization, viewDefinitionId, viewDefinition }) => ({
            params: {
              virtualizationId: virtualization.name,
              viewDefinitionId,
            },
            state: {
              virtualization,
              viewDefinition,
            },
          })
        ),
      },
      importSource: {
        root: makeResolverNoParams(
          routes.virtualizations.virtualization.views.importSource.root
        ),
        selectConnection: makeResolver<{ virtualization: Virtualization }>(
          routes.virtualizations.virtualization.views.importSource
            .selectConnection,
          ({ virtualization }) => ({
            params: {
              virtualizationId: virtualization.name,
            },
            state: {
              virtualization,
            },
          })
        ),
        selectViews: makeResolver<{
          connectionId: string;
          virtualization: Virtualization;
        }>(
          routes.virtualizations.virtualization.views.importSource.selectViews,
          ({ connectionId, virtualization }) => ({
            params: {
              virtualizationId: virtualization.name,
            },
            state: {
              connectionId,
              virtualization,
            },
          })
        ),
      },
    },
    versions: makeResolver<{ virtualization: Virtualization }>(
      routes.virtualizations.virtualization.versions,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.name,
        },
        state: {
          virtualization,
        },
      })
    ),
    metrics: makeResolver<{ virtualization: Virtualization }>(
      routes.virtualizations.virtualization.metrics,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.name,
        },
        state: {
          virtualization,
        },
      })
    ),
    dataPermission: makeResolver<{ virtualization: Virtualization }>(
      routes.virtualizations.virtualization.dataPermission,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.name,
        },
        state: {
          virtualization,
        },
      })
    ),
    sqlClient: makeResolver<{ virtualization: Virtualization }>(
      routes.virtualizations.virtualization.sqlClient,
      ({ virtualization }) => ({
        params: {
          virtualizationId: virtualization.name,
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
