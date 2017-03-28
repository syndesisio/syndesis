import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';

import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subject } from 'rxjs/Subject';
import { plural } from 'pluralize';

import { RESTService } from './rest.service';
import { BaseEntity } from '../../model';

import { log, getCategory } from '../../logging';
import { EventsService, ChangeEvent } from './events.service';

const category = getCategory('AbstractStore');

export abstract class AbstractStore<T extends BaseEntity, L extends Array<T>,
  R extends RESTService<T, L>> {

  private _list: BehaviorSubject<L>;

  private _current: BehaviorSubject<T>;

  private _loading: BehaviorSubject<boolean> = new BehaviorSubject(false);

  private changeEvents: Observable<ChangeEvent>;

  constructor(private service: R, private eventService: EventsService, initialList: L, initialCurrent: T) {
    this._list = new BehaviorSubject<L>(initialList);
    this._current = new BehaviorSubject<T>(initialCurrent);

    this.changeEvents = this.eventService.changeEvents.filter((x) => {
      return x.kind === this.service.kind;
    });

  }

  protected abstract get kind(): string;

  get list() {
    // Give back the _list,
    // but also update it if we get notified the a change occurred.
    return Observable.merge(
      this._list,
      this.changeEvents.flatMap((event) => {
        // We could probably get fancy one day an only fetch the entry that matches event.id
        return this.service.list();
      }),
    ).share();
  }

  get resource() {
    return this._current.asObservable();
  }

  get loading() {
    return this._loading.asObservable();
  }

  loadAll(retries = 0) {
    this._loading.next(true);
    this.service.list().subscribe(
      (list) => {
        this._list.next(list);
        this._loading.next(false);
      },
      (error) => {
        error = this.massageError(error);
        log.debugc(() => 'Error retrieving ' + plural(this.kind) + ': ' + error, category);
        if (retries < 3) {
          setTimeout(() => {
            this.loadAll(retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._loading.next(false);
        }
      });
  }

  newInstance(): T {
    throw new Error('No `newInstance()` defined for ' + this.kind);
  }

  loadOrCreate(id?: string) {
    if (id) {
      this.load(id);
    } else {
      // This should happen async too for consistency
      setTimeout(() => {
        this._current.next(this.newInstance());
        this._loading.next(false);
      }, 1);
    }
  }

  private plain(entity: T): T {
    if ('plain' in entity) {
      return (<any>entity).plain();
    } else {
      return entity;
    }
  }

  load(id: string, retries = 0) {
    this._loading.next(true);
    this.service.get(id).subscribe(
      (entity) => {
        this._current.next(this.plain(entity));
        this._loading.next(false);
      },
      (error) => {
        error = this.massageError(error);
        log.debugc(() => 'Error retrieving ' + this.kind + ': ' + error, category);
        if (retries < 3) {
          setTimeout(() => {
            this.load(id, retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._loading.next(false);
        }
      });
  }

  private massageError(error: any) {
    console.log("Error (in store): ", error);
    switch (typeof error) {
      case 'object':
        return error;
      case 'string':
        try {
          return JSON.parse(error);
        } catch(err) {
          // some random text back from the server :-(
          return { error: error };
        }
      default:
        return { error: error };
    }
  }

  create(entity: T): Observable<T> {
    const created = new Subject<T>();
    this.service.create(entity).subscribe(
      (e) => {
        created.next(this.plain(e));
      },
      (error) => {
        error = this.massageError(error);
        log.debugc(() => 'Error creating ' + this.kind + ' (' + JSON.stringify(entity, null, 2) + ')' + ': ' + error, category);
        created.error(error);
      });
    return created.share();
  }

  update(entity: T): Observable<T> {
    const updated = new Subject<T>();
    this.service.update(entity).subscribe(
      (e) => {
        updated.next(this.plain(e));
      },
      (error) => {
        error = this.massageError(error);
        log.debugc(() => 'Error updating ' + this.kind + ' (' + JSON.stringify(entity, null, 2) + ')' + ': ' + error, category);
        updated.error(error);
      });
    return updated.share();
  }

  updateOrCreate(entity: T): Observable<T> {
    if (entity.id) {
      return this.update(entity);
    } else {
      return this.create(entity);
    }
  }

  /*
   deleteEntity(id?: string) {
   if(id) {
   this.service.delete(id);
   }
   }
   */

}
