import { Integration } from '@syndesis/models';

export const serializeIntegration = (i: Integration) =>
  btoa(encodeURIComponent(JSON.stringify(i)));
