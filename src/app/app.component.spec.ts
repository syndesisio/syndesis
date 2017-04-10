/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { RestangularModule } from 'ng2-restangular';
import { OAuthModule } from 'angular-oauth2-oidc-hybrid';

import { CollapseModule, DropdownModule, ModalModule } from 'ng2-bootstrap';

import { AppComponent } from './app.component';
import { ConfigService } from './config.service';
import { UserService } from './common/user.service';
import { StoreModule } from './store/store.module';

import { TestSupportService } from './store/test-support.service';

describe('AppComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RestangularModule,
        StoreModule,
        ModalModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        OAuthModule.forRoot(),
        CollapseModule.forRoot(),
        DropdownModule.forRoot(),
      ],
      providers: [
        ConfigService,
        UserService,
        TestSupportService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http, useFactory: (backend, options) => {
            return new Http(backend, options);
          }, deps: [MockBackend, RequestOptions],
        },
      ],
      declarations: [
        AppComponent,
      ],
    });
    TestBed.compileComponents();
  });

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  it(`should have as title 'Red Hat iPaaS'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.title).toEqual('Red Hat iPaaS');
  }));

});
