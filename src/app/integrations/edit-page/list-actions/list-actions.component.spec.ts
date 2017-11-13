/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { SyndesisCommonModule } from '../../../common/common.module';
import { ListActionsComponent } from './list-actions.component';
import { StoreModule } from '../../../store/store.module';

describe('ListActionsComponent', () => {
  let component: ListActionsComponent;
  let fixture: ComponentFixture<ListActionsComponent>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          SyndesisCommonModule,
          RouterTestingModule.withRoutes([]),
          StoreModule
        ],
        declarations: [ListActionsComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ListActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
