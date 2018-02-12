import { Type } from '@angular/core';
import { Endpoints } from '@syndesis/ui/platform';

import { integrationEndpoints } from './integration';

export const platformEndpoints: Endpoints = {
  ...integrationEndpoints
};
