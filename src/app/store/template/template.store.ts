import { Injectable } from '@angular/core';
import { TemplateService } from './template.service';
import { IntegrationTemplate } from '../../model';

import { AbstractStore } from '../entity/entity.store';

@Injectable()
export class TemplateStore extends AbstractStore<IntegrationTemplate, Array<IntegrationTemplate>, TemplateService> {
  constructor(integrationService: TemplateService) {
    super(integrationService, [], <IntegrationTemplate>{});
  }

  protected get kind() { return 'Template'; }
}
