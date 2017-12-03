// /* tslint:disable:no-unused-variable */
// import { async, ComponentFixture, TestBed } from '@angular/core/testing';
// import { RouterTestingModule } from '@angular/router/testing';
// import { MockBackend } from '@angular/http/testing';
// import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
// import { RestangularModule } from 'ngx-restangular';

// import { ModalModule } from 'ngx-bootstrap/modal';
// import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
// import { NotificationModule } from 'patternfly-ng';
// import { ToolbarModule } from 'patternfly-ng';
// import { StoreModule } from '../../store/store.module';

// import { SyndesisCommonModule } from '../../common/common.module';
// import { PatternflyUIModule } from '../../common/ui-patternfly/ui-patternfly.module';
// import { ConnectionsListPage } from './list-page.component';
// import { ConnectionsListComponent } from '../list/list.component';

// describe('ConnectionListPage', () => {
//   let component: ConnectionsListPage;
//   let fixture: ComponentFixture<ConnectionsListPage>;

//   beforeEach(
//     async(() => {
//       TestBed.configureTestingModule({
//         imports: [
//           SyndesisCommonModule.forRoot(),
//           StoreModule,
//           RouterTestingModule.withRoutes([]),
//           RestangularModule.forRoot(),
//           ModalModule.forRoot(),
//           BsDropdownModule.forRoot(),
//           NotificationModule,
//           PatternflyUIModule,
//         ],
//         declarations: [
//           ConnectionsListPage,
//           ConnectionsListComponent,
//         ],
//         providers: [
//           MockBackend,
//           { provide: RequestOptions, useClass: BaseRequestOptions },
//           {
//             provide: Http,
//             useFactory: (backend, options) => {
//               return new Http(backend, options);
//             },
//             deps: [MockBackend, RequestOptions],
//           },
//         ],
//       }).compileComponents();
//     }),
//   );

//   beforeEach(() => {
//     fixture = TestBed.createComponent(ConnectionsListPage);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });
// });
