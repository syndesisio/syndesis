import { async, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Http, RequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CollapseModule, ModalModule } from 'ngx-bootstrap';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { RestangularModule } from 'ngx-restangular';
import { NotificationModule } from 'patternfly-ng';

import { AppComponent } from './app.component';
import { SyndesisCommonModule } from './common/common.module';
import { NavigationService } from './common/navigation.service';
import { UserService } from '@syndesis/ui/platform';
import { ConfigService } from './config.service';
import { StoreModule } from './store/store.module';
import { TestSupportService } from './store/test-support.service';
import { TourNgxBootstrapModule } from 'ngx-tour-ngx-bootstrap';

/* tslint:disable:no-unused-variable */

describe('AppComponent', () => {
  const APP_NAME = 'Syndesis';

  beforeEach(() => {
    const configServiceStub = {
      getSettings: (...params) => APP_NAME
    };

    TestBed.configureTestingModule({
      imports: [
        RestangularModule,
        StoreModule,
        SyndesisCommonModule.forRoot(),
        ModalModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        CollapseModule.forRoot(),
        BsDropdownModule.forRoot(),
        NotificationModule,
        TourNgxBootstrapModule.forRoot()
      ],
      providers: [
        ConfigService,
        UserService,
        TestSupportService,
        NavigationService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http,
          useFactory: (backend, options) => {
            return new Http(backend, options);
          },
          deps: [MockBackend, RequestOptions]
        },
        { provide: ConfigService, useValue: configServiceStub }
      ],
      declarations: [AppComponent]
    });
    TestBed.compileComponents();
  });

  it(
    'should create the app',
    async(() => {
      const fixture = TestBed.createComponent(AppComponent);
      const app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
    })
  );

  it(
    `should expose the application property name as '${APP_NAME}'`,
    async(() => {
      const fixture = TestBed.createComponent(AppComponent);
      const app = fixture.debugElement.componentInstance;
      expect(app.appName).toEqual(APP_NAME);
    })
  );
});
