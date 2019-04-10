import { Component, Input, Output, EventEmitter } from '@angular/core';

import { Connection, I18NService } from '@syndesis/ui/platform';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';

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
        <strong>{{ 'shared.description' | synI18n }}:</strong>
      </div>
      <div class="col-sm-10">
        <syndesis-editable-textarea [value]="connection.description"
                                    placeholder="No description set..."
                                    (onSave)="onAttributeUpdated('description', $event)"></syndesis-editable-textarea>
      </div>
    </div>
    <div class="row syn-connection-detail-info__usage">
      <div class="col-sm-2">
        <strong>{{ 'shared.usage' | synI18n }}:</strong>
      </div>
      <div class="col-sm-10">
        {{ usageText }}
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
    private i18nService: I18NService
  ) {}

  onAttributeUpdated(attr: string, value) {
    this.connection[attr] = value;
    this.updated.emit(this.connection);
  }

  get usageText(): string {
    if ( this.connection.uses ) {
      if ( this.connection.uses === 1 ) {
        return this.i18nService.localize( 'connections.used-once-msg' );
      }

      if ( this.connection.uses > 1 ) {
        return this.i18nService.localize( 'connections.used-multi-msg', [ this.connection.uses ] );
      }
    }

    return this.i18nService.localize( 'connections.not-used-msg' );
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
