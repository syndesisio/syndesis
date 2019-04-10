import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { CoreModule } from '@syndesis/ui/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { ConnectionsListComponent } from '@syndesis/ui/connections/list/list.component';
import { TestApiModule } from '@syndesis/ui/api/testing';
import { PlatformModule } from '@syndesis/ui/platform';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

describe('ConnectionsListComponent', () => {
  let component: ConnectionsListComponent;
  let fixture: ComponentFixture<ConnectionsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestApiModule,
        CommonModule,
        SyndesisCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        SyndesisStoreModule,
        PlatformModule.forRoot(),
        CoreModule.forRoot(),
        VendorModule,
      ],
      declarations: [ConnectionsListComponent],
      providers: [EVENTS_SERVICE_MOCK_PROVIDER],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
