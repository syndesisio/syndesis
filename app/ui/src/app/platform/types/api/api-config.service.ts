import { Injectable } from '@angular/core';

import { Endpoints } from './api.models';

@Injectable()
export abstract class ApiConfigService {
  /**
   * The base url, encompassing the hostname and the REST API host directory
   *
   * Eg. 'https://syndesis.192.168.64.17.xip.io/api/v2'
   */
  abstract baseUrl: string;

  /**
   * A StringMap of key/value pairs of unique strings mapped to the different API endpoints
   * available. These can be defined including either index-zero placeholders or named placeholders.
   *
   * Eg. { 'myEndpoint': '/exployees/list/{role}/{name}' }
   */
  abstract endpoints: Endpoints;
}
