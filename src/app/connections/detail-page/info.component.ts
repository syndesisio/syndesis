import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Connection } from '../../model';
import { ConnectionService } from '../../store/connection/connection.service';
import { ConnectionConfigurationService } from '../common/configuration/configuration.service';

@Component({
  selector: 'syndesis-connection-detail-info',
  template: `
    <h1>
      <dl class="dl-horizontal">
        <dt>
          <img src="../../../assets/icons/{{ connection.connectorId }}.connection.png" height="46" width="46">
        </dt>
        <dd>
          <syndesis-editable-text [value]="connection.name"
                                  [validationFn]="validateName"
                                  (onSave)="onAttributeUpdated('name', $event)"></syndesis-editable-text>
        </dd>
      </dl>
    </h1>
    <dl class="dl-horizontal">
      <dt>
        Tags:
      </dt>
      <dd>
        <syndesis-editable-tags [value]="connection.tags"
                                placeholder="No tags set..."
                                (onSave)="onAttributeUpdated('tags', $event)"></syndesis-editable-tags>
      </dd>
      <dt>
        Description:
      </dt>
      <dd>
        <syndesis-editable-textarea [value]="connection.description"
                                    placeholder="No description set..."
                                    (onSave)="onAttributeUpdated('description', $event)"></syndesis-editable-textarea>
      </dd>
    </dl>
  `,
  styles: [`
    h1 dt { width: 46px; }
    h1 dd { margin-left: 66px; }
    dt { text-align: left; width: 120px; }
    dd { margin-left: 140px; }
    dt:not(:first-child), dd:not(:first-child) { margin-top: 10px; }
  `],
})
export class ConnectionDetailInfoComponent {

  @Input() connection: Connection;
  @Output() updated = new EventEmitter<Connection>();

  constructor(
    private connectionService: ConnectionService,
    private configurationService: ConnectionConfigurationService,
  ) {}

  onAttributeUpdated(attr: string, value) {
    this.connection[attr] = value;
    this.updated.emit(this.connection);
  }

  validateName = (name: string) => {
    if (name === '') {
      return 'Name is required';
    } else {
      const connection = this.configurationService.cloneObject(this.connection);
      connection.name = name;
      return this.connectionService.validate(connection)
        .toPromise()
        .then(response => null)
        .catch(response => {
          const nameTaken = response.data.filter(item => item.error === 'UniqueProperty').length > 0;
          return nameTaken ? 'That name is taken. Try another.' : null;
        });
    }
  }

}
