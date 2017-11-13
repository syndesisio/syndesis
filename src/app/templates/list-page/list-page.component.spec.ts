/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';
import { RouterTestingModule } from '@angular/router/testing';

import { SyndesisCommonModule } from '../../common/common.module';
import { TemplatesListPage } from './list-page.component';
import { TemplatesListComponent } from '../list/list.component';
import { ListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { StoreModule } from '../../store/store.module';

describe('TemplatesListPage', () => {
  let component: TemplatesListPage;
  let fixture: ComponentFixture<TemplatesListPage>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          SyndesisCommonModule.forRoot(),
          StoreModule,
          RestangularModule.forRoot(),
          RouterTestingModule.withRoutes([])
        ],
        declarations: [
          TemplatesListPage,
          ListToolbarComponent,
          TemplatesListComponent
        ],
        providers: [
          MockBackend,
          { provide: RequestOptions, useClass: BaseRequestOptions },
          {
            provide: Http,
            useFactory: (backend, options) => {
              return new Http(backend, options);
            },
            deps: [MockBackend, RequestOptions]
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatesListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
