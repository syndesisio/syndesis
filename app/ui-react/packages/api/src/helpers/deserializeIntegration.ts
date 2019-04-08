import { Integration } from '@syndesis/models';

export const deserializeIntegration = (i: string) =>
  JSON.parse(decodeURIComponent(atob(i))) as Integration;
