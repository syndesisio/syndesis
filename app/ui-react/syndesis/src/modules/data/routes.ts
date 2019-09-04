import { include } from 'named-urls';

export default include('/data', {
  root: '',
  virtualizations: include('virtualizations', {
    create: 'create',
    import: 'import',
    list: '',
    virtualization: include(':virtualizationId', {
      metrics: 'metrics',
      relationship: 'relationship',
      root: '',
      sqlClient: 'sqlClient',
      views: include('views', {
        createView: include('createView', {
          root: '',
          selectName: 'selectName',
          selectSources: 'selectSources',
        }),
        edit: include(':viewDefinitionId', {
          criteria: 'criteria',
          groupBy: 'groupBy',
          join: 'join',
          properties: 'properties',
          root: '',
          sql: 'sql',
        }),
        importSource: include('importSource', {
          root: '',
          selectConnection: 'selectConnection',
          selectViews: 'selectViews',
        }),
        root: '',
      }),
    }),
  }),
});
