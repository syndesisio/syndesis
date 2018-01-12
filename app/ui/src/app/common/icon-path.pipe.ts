import { Pipe, PipeTransform, ChangeDetectorRef } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { ConfigService } from '@syndesis/ui/config.service';

interface IconConnection {
  icon: string | File;
  connectorId?: string;
  id?: string;
}

@Pipe({
  name: 'synIconPath'
})
export class IconPathPipe implements PipeTransform {
  private apiEndpoint: string;

  constructor(
    private configService: ConfigService,
    private sanitizer: DomSanitizer,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    this.apiEndpoint = this.configService.getSettings().apiEndpoint;
  }

  transform(connection: IconConnection): SafeUrl | null {
    if (connection && connection.icon instanceof File) {
      const promise = new Promise<SafeUrl>(resolve => {
        const file = connection.icon as File;
        const fileReader = new FileReader();

        fileReader.onload = event => resolve(this.toSafeUrl(event.target['result']));
        fileReader.readAsDataURL(file);
      });

      const asyncPipe = new AsyncPipe(this.changeDetectorRef);
      return asyncPipe.transform<SafeUrl>(promise);

    } else if (connection && typeof (connection.icon) === 'string') {
      const connectorId = connection.connectorId || connection.id;
      let iconPath = `~@syndesis/assets/icons/${connectorId}.integration.png`;

      if (connection.icon.toLowerCase().startsWith('db:')) {
        iconPath = `${this.apiEndpoint}/connectors/${connectorId}/icon`;
      }

      return this.toSafeUrl(iconPath);
    }

    return;
  }

  private toSafeUrl(iconPath: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(iconPath);
  }
}
