import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Step } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';
import { ENDPOINT } from '@syndesis/ui/store';

interface IconConnection {
  icon: string | File;
  iconFile?: File;
  connectorId?: string;
  id?: string;
}

@Pipe({
  name: 'synIconPath',
})
export class IconPathPipe implements PipeTransform {
  private apiEndpoint: string;

  constructor(
    private configService: ConfigService,
    private sanitizer: DomSanitizer
  ) {
    this.apiEndpoint = this.configService.getSettings().apiEndpoint;
  }

  transform(
    thing: IconConnection | Step,
    isConnector?: boolean
  ): SafeUrl | null {
    if (typeof (thing as Step).stepKind !== 'undefined') {
      if ((thing as Step).stepKind === ENDPOINT) {
        return this.transformConnection(
          (thing as Step).connection,
          isConnector
        );
      } else {
        return this.transformStep(thing as Step);
      }
    } else {
      return this.transformConnection(thing as IconConnection, isConnector);
    }
  }

  private transformStep(step: Step): SafeUrl | null {
    if (!step) {
      return this.toSafeUrl('');
    }
    if (step.extension) {
      return this.toSafeUrl(step.extension.icon);
    }
    const iconPath = `./../../assets/icons/steps/${step.stepKind}.svg`;
    return this.toSafeUrl(iconPath);
  }

  private transformConnection(
    connection: IconConnection,
    isConnector?: boolean
  ): SafeUrl | null {
    if (
      connection &&
      (connection.icon instanceof File || connection.iconFile)
    ) {
      const file = connection.iconFile || connection.icon;
      const tempIconBlobPath = URL.createObjectURL(file);
      return this.toSafeUrl(tempIconBlobPath);
    } else if (connection && typeof connection.icon === 'string') {
      // TODO: Streamline this assignation block once we manage to create a common model
      //       schema for entities featuring icons, so we can remove all these conditional logic
      let connectionId = connection.connectorId || connection.id;
      const defaultIcon = isConnector ? connection.icon : connectionId;
      const defaultIconSuffix = isConnector ? 'connection' : 'integration';
      let iconPath = `./../../assets/icons/${defaultIcon}.${defaultIconSuffix}.png`;

      if (connection.icon.toLowerCase().startsWith('data:')) {
        return this.toSafeUrl(connection.icon);
      }

      if (
        connection.icon.toLowerCase().startsWith('db:') ||
        connection.icon.startsWith('extension:')
      ) {
        connectionId = isConnector ? connection.id : connectionId;
        iconPath = `${this.apiEndpoint}/connectors/${connectionId}/icon?${
          connection.icon
        }`;
      }

      return this.toSafeUrl(iconPath);
    }

    return this.toSafeUrl('');
  }

  private toSafeUrl(iconPath: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(iconPath);
  }
}
