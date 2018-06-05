import { Component, Input, OnInit } from '@angular/core';
import {
  LeveledMessage,
  MessageLevel,
  StatusCodeDecoderService
} from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-inline-alert',
  templateUrl: './inline-alert.component.html',
  styleUrls: []
})
export class InlineAlertComponent implements OnInit {
  icon: string[];
  messageString: any;
  alertLevel: string;
  @Input() message: LeveledMessage;

  constructor(private statusCodeDecoderService: StatusCodeDecoderService) {}

  ngOnInit() {
    if (!this.message) {
      return;
    }
    this.messageString = this.statusCodeDecoderService.getMessageString(
      this.message
    );
    switch (this.message.level) {
      case MessageLevel.WARN:
        this.alertLevel = 'alert-warning';
        this.icon = ['pficon', 'pficon-warning-triangle-o'];
        break;
      case MessageLevel.ERROR:
        this.alertLevel = 'alert-danger';
        this.icon = ['pficon', 'pficon-error-circle-o'];
        break;
      default:
        this.alertLevel = 'alert-info';
        this.icon = ['pficon', 'pficon-info'];
    }
  }
}
