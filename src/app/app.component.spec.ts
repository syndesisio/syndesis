import { async, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Http, RequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BsDropdownModule, CollapseModule, ModalModule } from 'ngx-bootstrap';
import { RestangularModule } from 'ngx-restangular';
import { NotificationModule } from 'patternfly-ng';

import { AppComponent } from './app.component';
import { SyndesisCommonModule } from './common/common.module';
import { NavigationService } from './common/navigation.service';
import { UserService } from './common/user.service';
import { ConfigService } from './config.service';
import { StoreModule } from './store/store.module';
import { TestSupportService } from './store/test-support.service';

/* tslint:disable:no-unused-variable */

describe('AppComponent', () => {
  beforeEach(() => {
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
          deps: [MockBackend, RequestOptions],
        },
      ],
      declarations: [AppComponent],
    });
    TestBed.compileComponents();
  });

  it(
    'should create the app',
    async(() => {
      const fixture = TestBed.createComponent(AppComponent);
      const app = fixture.debugElement.componentInstance;
      expect(app).toBeTruthy();
    }),
  );

  it(
    `should have as title 'Syndesis'`,
    async(() => {
      const fixture = TestBed.createComponent(AppComponent);
      const app = fixture.debugElement.componentInstance;
      expect(app.title).toEqual('Syndesis');
    }),
  );
});
