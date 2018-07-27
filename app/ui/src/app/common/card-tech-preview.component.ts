import { Component, Input, OnInit, OnDestroy, HostBinding, ViewEncapsulation } from '@angular/core';
import { Connection, Connector } from '@syndesis/ui/platform';
import { Subscription } from 'rxjs';
import { ConnectorStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-card-tech-preview',
  template: `
    <div *ngIf="isTechPreview"
          class="syn-card-info text-right"
          data-toggle="tooltip"
          [title]="'connections.tech-preview-tooltip' | synI18n">
            {{ 'connections.tech-preview' | synI18n }}
            <span class="pficon pficon-info"></span>
    </div>
  `,
  styleUrls: ['./card-tech-preview.component.scss']
})
export class CardTechPreviewComponent implements OnInit {
  @Input() item: Connection | Connector;
  @HostBinding('attr.is-tech-preview')
  isTechPreview: boolean;

  ngOnInit(): void {
    const connector = CardTechPreviewComponent.determineConnector(this.item);

    if (connector) {
      this.isTechPreview = CardTechPreviewComponent.taggedAsTechPreview(connector);
    }
  }

  private static determineConnector(connectionOrConnector: Connection | Connector) {
    if ((<Connection>connectionOrConnector).connector) {
      const connection = <Connection>connectionOrConnector;
      return connection.connector;
    }

    const connector = <Connector>connectionOrConnector;
    return connector;
  }

  private static taggedAsTechPreview(connector: Connector) {
    return connector.metadata && connector.metadata['tech-preview'] === 'true';
  }
}
