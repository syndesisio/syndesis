import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';

import { BaseEntity } from '@syndesis/ui/platform';
import {
  Integration,
  Integrations,
  IntegrationMetrics
} from '@syndesis/ui/platform/types/integration/integration.models';

@Injectable()
export abstract class IntegrationService {
  /**
   * Sends a GET request to fetch all existing integration objects
   * @returns {Observable<Integrations>} List of existing integrations
   */
  abstract fetch(): Observable<Integrations>;

  /**
   * Expects a new Integration object and submits it thru a POST request to the REST API
   * @param {Integration} integration The Integration object to create
   * @returns {Observable<any>} Whatever the API returns for this - TODO: Properly model this
   */
  abstract create(integration: Integration): Observable<any>;

  /**
   * Updates an existing Integration object through a PUT request against the REST API
   * @param {Integration} integration The Integration object to update
   * @returns {Observable<any>} Whatever the API returns for this - TODO: Properly model this
   */
  abstract update(integration: Integration): Observable<any>;

  /**
   * Deletes an existing Integration object through a DELETE request against the REST API
   * @param {Integration} integration The Integration object to delete or an object literal exposing the `id`
   * of the Integration to delete. Eg: { id: 345345 }.
   * @returns {Observable<any>} Whatever the API returns for this - TODO: Properly model this
   */
  abstract delete(integration: Integration | BaseEntity): Observable<any>;

  /**
   * Fetch monitoring metrics for a given Integration or for the entire
   * Integrations collection when no ID is provided
   * @param {string} [id] ID of the specific Integration we want to fetch metrics from
   * @returns {Observable<IntegrationMetrics>} Object representing different monitoring parameters for a particular integration or all
   */
  abstract fetchMetrics(id?: string): Observable<IntegrationMetrics>;
}
