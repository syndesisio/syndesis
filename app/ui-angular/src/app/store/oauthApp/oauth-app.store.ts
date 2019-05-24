import { Injectable } from '@angular/core';
import { OAuthAppService } from '@syndesis/ui/store/oauthApp/oauth-app.service';
import { OAuthApps, OAuthApp } from '@syndesis/ui/settings';
import { AbstractStore } from '@syndesis/ui/store/entity/entity.store';
import { EventsService } from '@syndesis/ui/store/entity/events.service';

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

  loadAll() {
    // Force a reload as no events get generated for oauth apps
    super.loadAll(1);
  }
}
