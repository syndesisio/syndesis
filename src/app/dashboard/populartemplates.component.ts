import { Component, Input } from '@angular/core';

import { Templates } from '../store/template/template.model';

@Component({
  selector: 'ipaas-popular-templates',
  templateUrl: './populartemplates.component.html',
  styleUrls: [],
})
export class PopularTemplatesComponent {

  @Input() templates: Templates;

  @Input() loading: boolean;

}
