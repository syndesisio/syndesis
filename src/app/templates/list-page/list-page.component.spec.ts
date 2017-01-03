/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StoreModule } from '@ngrx/store';

import { TemplatesListPage } from './list-page.component';
import { TemplatesListComponent } from '../list/list.component';
import { ListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { reducers } from '../../store/store';

describe('TemplatesListPage', () => {
  let component: TemplatesListPage;
  let fixture: ComponentFixture<TemplatesListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.provideStore(reducers)],
      declarations: [TemplatesListPage, ListToolbarComponent, TemplatesListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatesListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
