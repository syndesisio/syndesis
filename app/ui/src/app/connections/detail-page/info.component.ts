import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription } from 'rxjs';

import { Connection } from '@syndesis/ui/platform';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';
import { ConnectionConfigurationService } from '@syndesis/ui/connections/common/configuration/configuration.service';

@Component({
  selector: 'syndesis-connection-detail-info',
  template: `
    <h1 class="syn-connection-detail-info__header">
      <img [src]="connection | synIconPath">
      <syndesis-editable-text [value]="connection.name"
                              [validationFn]="validateName"
                              (onSave)="onAttributeUpdated('name', $event)"></syndesis-editable-text>
    </h1>
    <div class="row syn-connection-detail-info__description">
      <div class="col-sm-2">
        <strong>Description:</strong>
      </div>
      <div class="col-sm-10">
        <syndesis-editable-textarea [value]="connection.description"
                                    placeholder="No description set..."
                                    (onSave)="onAttributeUpdated('description', $event)"></syndesis-editable-textarea>
      </div>
    </div>
    <div *ngIf="connection.board?.messages && connection.board?.messages.length">
      <div *ngFor="let message of connection.board.messages">
        <syndesis-inline-alert [message]="message"></syndesis-inline-alert>
      </div>
    </div>
  `,
  styleUrls: ['./info.component.scss']
})
export class ConnectionDetailInfoComponent {
  @Input() connection: Connection;
  @Output() updated = new EventEmitter<Connection>();

  constructor(
    private connectionService: ConnectionService,
    private configurationService: ConnectionConfigurationService
  ) {}

  onAttributeUpdated(attr: string, value) {
    this.connection[attr] = value;
    this.updated.emit(this.connection);
  }

  validateName = (name: string) => {
    if (name === '') {
      return 'Name is required';
    } else if (name !== this.connection.name) {
      return this.connectionService
        .validateName(name)
        .then(validationErrors => {
          if (validationErrors && validationErrors.UniqueProperty) {
            return 'That name is taken. Try another.';
          } else {
            return validationErrors;
          }
        });
    }
  }
}
