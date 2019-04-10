import { Component, Input, OnInit, HostBinding } from '@angular/core';
import { Connection, Connector } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-card-tech-preview',
  template: `
    <ng-template #synTechPreviewInfo>
      <a rel="nofollow" role="link" target="_blank" href="{{ 'connections.tech-preview-link' | synI18n }}">
        {{ 'connections.tech-preview-link-text' | synI18n }}</a>
      {{ 'connections.tech-preview-info' | synI18n }}
      <a href="mailto:{{ 'shared.email' | synI18n}}">{{ 'shared.email' | synI18n }}</a>.
    </ng-template>
    <div *ngIf="isTechPreview"
          class="syn-card-info text-right">
            {{ 'connections.tech-preview' | synI18n }}
            <span class="pficon pficon-info"
                  outsideClick="true"
                  placement="left"
                  container="body"
                  [popover]="synTechPreviewInfo"></span>
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
