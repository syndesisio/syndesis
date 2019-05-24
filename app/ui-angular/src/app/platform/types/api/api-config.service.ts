import { Injectable } from '@angular/core';

import { Endpoints } from '@syndesis/ui/platform/types/api/api.models';

@Injectable()
export abstract class ApiConfigService {
  /**
   * The base url, encompassing the hostname and the REST API host directory
   * @example 'https://syndesis.192.168.64.17.xip.io/api/v2'
   */
  abstract baseUrl: string;

  /**
   * A StringMap of key/value pairs of unique strings mapped to the different API endpoints
   * available. These can be defined including either index-zero placeholders or named placeholders.
   * @example { 'myEndpoint': '/exployees/list/{role}/{name}' }
   */
  abstract endpoints: Endpoints;

  /**
   * Registers additional endpoints in the application global endpoint set.
   * Mostly used to add new endpoints in lazy loaded modules upon request.
   * @param endpoints A stringmap of additional endpoints to be added to the global applications' endpoints catalogue
   */
  abstract registerEndpoints(endpoints: Endpoints): void;
}
