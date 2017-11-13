import { Component, Input } from '@angular/core';
import { Connection } from '../../model';

@Component({
  selector: 'syndesis-connection-detail-breadcrumb',
  template: `
    <div class="row toolbar-pf">
      <div class="col-sm-12">
          <ol class="breadcrumb">
            <li>
              <a [routerLink]=" ['/'] ">Home</a>
            </li>
            <li>
              <a [routerLink]=" ['/connections'] ">Connections</a>
            </li>
            <li class='active'><strong>Connection Details</strong></li>
          </ol>
      </div>
    </div>
  `,
  styles: [
    `
    .toolbar-pf {
      background: inherit;
    }
    .toolbar-pf ol {
      display: inline-block;
      margin-bottom: 6px;
    }
  `
  ]
})
export class ConnectionDetailBreadcrumbComponent {}
