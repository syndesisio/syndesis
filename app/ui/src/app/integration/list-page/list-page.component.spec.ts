/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { VendorModule } from '@syndesis/ui/vendor';

import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { PatternflyUIModule } from '@syndesis/ui/common/ui-patternfly/ui-patternfly.module';
import { IntegrationListModule } from '@syndesis/ui/integration/list/list.module';
import { IntegrationListPage } from '@syndesis/ui/integration/list-page/list-page.component';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';

xdescribe('IntegrationsListPage', () => {
  let component: IntegrationListPage;
  let fixture: ComponentFixture<IntegrationListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SyndesisCommonModule.forRoot(),
        SyndesisStoreModule,
        RouterTestingModule.withRoutes([]),
        VendorModule,
        PatternflyUIModule,
        IntegrationListModule
      ],
      declarations: [IntegrationListPage]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
