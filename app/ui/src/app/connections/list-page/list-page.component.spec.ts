import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { CoreModule } from '@syndesis/ui/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { TestApiModule } from '@syndesis/ui/api/testing';

import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { PatternflyUIModule } from '@syndesis/ui/common/ui-patternfly/ui-patternfly.module';
import { ConnectionsListPage } from '@syndesis/ui/connections/list-page/list-page.component';
import { ConnectionsListComponent } from '@syndesis/ui/connections/list/list.component';
import { PlatformModule } from '@syndesis/ui/platform';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

describe('ConnectionListPage', () => {
  let component: ConnectionsListPage;
  let fixture: ComponentFixture<ConnectionsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestApiModule,
        SyndesisCommonModule.forRoot(),
        PlatformModule.forRoot(),
        CoreModule.forRoot(),
        SyndesisStoreModule,
        RouterTestingModule.withRoutes([]),
        VendorModule,
        PatternflyUIModule,
      ],
      declarations: [ConnectionsListPage, ConnectionsListComponent],
      providers: [EVENTS_SERVICE_MOCK_PROVIDER],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
