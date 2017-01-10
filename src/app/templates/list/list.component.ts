import { Component, Input } from '@angular/core';

import { Templates } from '../../store/template/template.model';

@Component({
  selector: 'ipaas-templates-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class TemplatesListComponent {

  @Input() templates: Templates;

  @Input() loading: boolean;

}
