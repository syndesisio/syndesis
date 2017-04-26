import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Connection } from '../../model';
import { CurrentConnectionService } from '../create-page/current-connection';

import { log, getCategory } from '../../logging';

const category = getCategory('Connections');

@Component({
  selector: 'ipaas-connection-view-toolbar',
  templateUrl: './view-toolbar.component.html',
  styleUrls: ['./view-toolbar.component.scss'],
})
export class ConnectionViewToolbarComponent {

  @Input()
  mode = 'view';

  saving = false;

  @Output()
  modeChange = new EventEmitter<string>();

  @Input() connection: Connection;

  constructor(
    private current: CurrentConnectionService,
  ) { }

  doEdit() {
    this.mode = 'edit';
    this.modeChange.emit(this.mode);
  }

  cancel() {
    this.mode = 'view';
    this.modeChange.emit(this.mode);
  }

  doSave() {
    this.saving = true;
    this.current.events.emit({
      kind: 'connection-save-connection',
      connection: this.current.connection,
      action: (connection: Connection) => {
        this.saving = false;
        this.mode = 'view';
        this.modeChange.emit(this.mode);
      },
      error: (reason: any) => {
        this.saving = false;
        log.debugc(() => 'Error creating connection: ' + JSON.stringify(reason, undefined, 2), category);
      },
    });
  }

}
