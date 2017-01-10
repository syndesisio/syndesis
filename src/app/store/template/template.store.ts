import { Injectable } from '@angular/core';
import { TemplateService } from './template.service';
import { Templates, Template } from './template.model';

import { AbstractStore } from '../entity/entity.store';

@Injectable()
export class TemplateStore extends AbstractStore<Template, Templates, TemplateService> {
  constructor(integrationService: TemplateService) {
    super(integrationService, [], <Template>{});
  }

  protected get kind() { return 'Template'; }
}
