/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';

import { RestangularModule } from 'ngx-restangular';

import { UserProviderService } from './user-provider.service';
import { ApiModule } from '@syndesis/ui/api';
import { ConfigService } from '@syndesis/ui/config.service';

describe('UserProviderServiceProvider', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ApiModule.forRoot(),
        RestangularModule.forRoot()],
      providers: [UserProviderService, ConfigService]
    });
  });

  it(
    'should ...',
    inject([UserProviderService], (service: UserProviderService) => {
      expect(service).toBeTruthy();
    })
  );
});
