/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { UserService } from './user.service';

xdescribe('UserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService],
    });
  });

  it(
    'should ...',
    inject([UserService], (service: UserService) => {
      expect(service).toBeTruthy();
    }),
  );
});
