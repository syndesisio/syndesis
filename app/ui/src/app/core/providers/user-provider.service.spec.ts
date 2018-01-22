/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';

import { RestangularModule } from 'ngx-restangular';

import { UserProviderService } from './user-provider.service';

describe('UserProviderServiceProvider', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RestangularModule.forRoot()],
      providers: [UserProviderService]
    });
  });

  it(
    'should ...',
    inject([UserProviderService], (service: UserProviderService) => {
      expect(service).toBeTruthy();
    })
  );
});
