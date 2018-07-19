import { CoreModule } from '@syndesis/ui/core/core.module';
import { async, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CollapseModule, ModalModule } from 'ngx-bootstrap';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ToastNotificationListModule as NotificationModule } from 'patternfly-ng';
import { StoreModule as NgRxStoreModule } from '@ngrx/store';

import { AppComponent } from '@syndesis/ui/app.component';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { NavigationService } from '@syndesis/ui/common/navigation.service';
import { UserService, ApiHttpService, platformReducer } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { TestSupportService } from '@syndesis/ui/store/test-support.service';
import { ERROR_HANDLER_PROVIDERS } from '@syndesis/ui/error-handler';

describe('AppComponent', () => {
  const APP_NAME = 'Syndesis';

  beforeEach(() => {
    const configServiceStub = {
      getSettings: (...params) => APP_NAME
    };

    TestBed.configureTestingModule({
      imports: [
        CoreModule.forRoot(),
        SyndesisStoreModule,
        SyndesisCommonModule.forRoot(),
        ModalModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        CollapseModule.forRoot(),
        BsDropdownModule.forRoot(),
        NotificationModule,
        NgRxStoreModule.forRoot(platformReducer)
      ],
      providers: [
        ERROR_HANDLER_PROVIDERS,
        ConfigService,
        UserService,
        TestSupportService,
        NavigationService,
        ApiHttpService,
        { provide: ConfigService, useValue: configServiceStub }
      ],
      declarations: [AppComponent]
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
