import { include } from 'named-urls';

export default include('/extensions', {
  extension: include(':extensionId', {
    details: '',
    update: 'update',
  }),
  import: 'import',
  list: '',
});
