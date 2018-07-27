import { Component, Input, OnInit, OnDestroy, HostBinding, ViewEncapsulation } from '@angular/core';
import { Connection, Connector } from '@syndesis/ui/platform';
import { Subscription } from 'rxjs';
import { ConnectorStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-card-tech-preview',
  template: `
    <div *ngIf="isTechPreview"
          class="card-pf-heading syn-card-info"
          data-toggle="tooltip"
          [title]="'connections.tech-preview-tooltip' | synI18n">{{ 'connections.tech-preview' | synI18n }}</div>
  `,
  styleUrls: ['./card-tech-preview.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CardTechPreviewComponent implements OnInit, OnDestroy {
  @Input() item: Connection | Connector;
  @HostBinding('attr.is-tech-preview')
  isTechPreview: boolean;
  private connectorsSubscription: Subscription;

  constructor(
    private connectorStore: ConnectorStore
  ) {}

  ngOnInit(): void {
    const [connector, connectorId] = CardTechPreviewComponent.determineConnector(this.item);

    if (connector) {
      this.isTechPreview = CardTechPreviewComponent.taggedAsTechPreview(connector);
    } else if (connectorId) {
      this.connectorsSubscription = this.connectorStore.load(connectorId).subscribe(
        loaded => {
          this.isTechPreview = CardTechPreviewComponent.taggedAsTechPreview(loaded);
        });
    }
  }

  ngOnDestroy() {
    if (this.connectorsSubscription) {
      this.connectorsSubscription.unsubscribe();
    }
  }

  private static determineConnector(connectionOrConnector: Connection | Connector): [Connector, string] {
    if ((<Connection>connectionOrConnector).connector) {
      const connection = <Connection>connectionOrConnector;
      return [connection.connector, connection.connectorId];
    }

    const connector = <Connector>connectionOrConnector;
    return [connector, connector.id];
  }

  private static taggedAsTechPreview(connector: Connector) {
    return connector.tags && connector.tags.indexOf('tech-preview') >= 0;
  }
}
