import { Injectable } from '@angular/core';

import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { IntegrationTemplate } from '../../model';

@Injectable()
export class TemplateService extends RESTService<IntegrationTemplate, Array<IntegrationTemplate>> {

  constructor(restangular: Restangular) {
    super(restangular.service('integrationtemplates'), 'integrationtemplate');
  }

}
