import { TestBed, inject } from '@angular/core/testing';

import { ApiModule } from '@syndesis/ui/api';
import { ConfigService } from '@syndesis/ui/config.service';

import { UserProviderService } from '@syndesis/ui/core/providers/user-provider.service';

describe('UserProviderServiceProvider', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ApiModule.forRoot()],
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
