import { Injectable } from '@angular/core';

import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { Extension, Extensions } from '../../model';

@Injectable()
export class ExtensionService extends RESTService<Extension, Extensions> {
  constructor(restangular: Restangular) {
    super(restangular.service('../v1beta1/extensions'), 'extension');
  }
}
