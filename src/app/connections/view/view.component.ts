import { Component, Input, OnInit } from '@angular/core';

import { Connection } from '../../model';

@Component({
  selector: 'ipaas-connection-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss'],
})
export class ConnectionViewComponent implements OnInit {

  @Input() connection: Connection;
  mode: string = 'view';

  constructor() {

  }

  getFormFields(connection: Connection) {
    const answer = [];
    let formFields = undefined;
    try {
      formFields = JSON.parse(connection.configuredProperties);
    } catch (err) {
      // silently fail
    }
    if (formFields) {
      for (const key in formFields) {
        if (!formFields.hasOwnProperty(key)) {
          continue;
        }
        const field = formFields[key];
        field.name = key;
        answer.push(field);
      }
    }
    return answer;
  }

  ngOnInit() {
    this.connection = <Connection>{};
  }

}
