/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';

import { RestangularModule } from 'ngx-restangular';

import { UserService } from './user.service';

describe('UserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RestangularModule.forRoot()],
      providers: [UserService]
    });
  });

  it(
    'should ...',
    inject([UserService], (service: UserService) => {
      expect(service).toBeTruthy();
    })
  );
});
