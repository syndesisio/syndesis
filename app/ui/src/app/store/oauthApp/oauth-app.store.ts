import { Injectable } from '@angular/core';
import { OAuthAppService } from './oauth-app.service';
import { OAuthApps, OAuthApp } from '@syndesis/ui/settings';
import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class OAuthAppStore extends AbstractStore<
  OAuthApp,
  OAuthApps,
  OAuthAppService
> {
  constructor(oauthAppService: OAuthAppService, eventService: EventsService) {
    super(oauthAppService, eventService, [], {} as OAuthApp);
  }

  protected get kind() {
    return 'OAuthApp';
  }
}
