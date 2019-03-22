import { CoreModule } from '@syndesis/ui/core/core.module';
import { async, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ToastNotificationListModule as NotificationModule } from 'patternfly-ng';
import { ClickOutsideModule } from 'ng-click-outside';
import { StoreModule as NgRxStoreModule } from '@ngrx/store';

import { AppComponent } from '@syndesis/ui/app.component';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { NavigationService } from '@syndesis/ui/common/navigation.service';
import {
  UserService,
  ApiHttpService,
  platformReducer,
} from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { TestSupportService } from '@syndesis/ui/store/test-support.service';
import { ERROR_HANDLER_PROVIDERS } from '@syndesis/ui/error-handler';
import { EVENTS_SERVICE_MOCK_PROVIDER } from './store/entity/events.service.spec';

describe('AppComponent', () => {
  const APP_NAME = 'Syndesis';

  beforeEach(() => {
    const configServiceStub = {
      getSettings: (...params) => APP_NAME,
    };

    TestBed.configureTestingModule({
      imports: [
        CoreModule.forRoot(),
        ClickOutsideModule,
        SyndesisStoreModule,
        SyndesisCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        NotificationModule,
        NgRxStoreModule.forRoot(platformReducer),
      ],
      providers: [
        ERROR_HANDLER_PROVIDERS,
        ConfigService,
        UserService,
        TestSupportService,
        NavigationService,
        ApiHttpService,
        { provide: ConfigService, useValue: configServiceStub },
        EVENTS_SERVICE_MOCK_PROVIDER,
      ],
      declarations: [AppComponent],
    });
    TestBed.compileComponents();
  });

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  it(`should expose the application property name as '${APP_NAME}'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.appName).toEqual(APP_NAME);
  }));
});
