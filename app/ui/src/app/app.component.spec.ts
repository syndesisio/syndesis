import { CoreModule } from './core/core.module';
import { async, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CollapseModule, ModalModule } from 'ngx-bootstrap';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { NotificationModule } from 'patternfly-ng';
import { StoreModule as NgRxStoreModule } from '@ngrx/store';

import { AppComponent } from './app.component';
import { SyndesisCommonModule } from './common/common.module';
import { NavigationService } from './common/navigation.service';
import { UserService, ApiHttpService } from '@syndesis/ui/platform';
import { ConfigService } from './config.service';
import { SyndesisStoreModule } from './store/store.module';
import { TestSupportService } from './store/test-support.service';
import { platformReducer } from './platform';
import { ERROR_HANDLER_PROVIDERS } from './error-handler';

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
