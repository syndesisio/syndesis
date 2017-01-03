import { Injectable } from '@angular/core';

import { Restangular } from 'ng2-restangular';

import { RESTService } from '../entity/rest.service';
import { Template, Templates } from './template.model';

@Injectable()
export class TemplateService extends RESTService<Template, Templates> {

  constructor(restangular: Restangular) {
    super(restangular.service('integrationtemplates'));
  }

}
