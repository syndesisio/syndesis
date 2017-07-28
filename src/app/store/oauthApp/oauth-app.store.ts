import { Injectable } from '@angular/core';
import { OAuthAppService } from './oauth-app.service';
import { OAuthApps, OAuthApp, TypeFactory } from '../../model';
import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class OAuthAppStore extends AbstractStore<
  OAuthApp,
  OAuthApps,
  OAuthAppService
> {
  constructor(oauthAppService: OAuthAppService, eventService: EventsService) {
    super(oauthAppService, eventService, [], TypeFactory.createOAuthApp());
  }

  protected get kind() {
    return 'OAuthApp';
  }
}
