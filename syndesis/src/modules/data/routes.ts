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
      views: 'views',
    }),
  }),
});
