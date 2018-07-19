import { Type } from '@angular/core';
import { Endpoints } from '@syndesis/ui/platform';

import { integrationEndpoints } from '@syndesis/ui/platform/types/integration';

export const platformEndpoints: Endpoints = {
  ...integrationEndpoints
};
