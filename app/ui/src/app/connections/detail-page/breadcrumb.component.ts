import { Component } from '@angular/core';

@Component({
  selector: 'syndesis-connection-detail-breadcrumb',
  template: `
    <div class="syn-toolbar--inverted">
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
    </div>
  `,
  styles: [
    `
    .toolbar-pf ol {
      padding: 0;
      margin-bottom: 10px;
    }
  `
  ]
})
export class ConnectionDetailBreadcrumbComponent {}
