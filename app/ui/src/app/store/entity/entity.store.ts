import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';

import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { plural } from 'pluralize';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/share';

import { RESTService } from './rest.service';
import { BaseEntity } from '../../model';

import { log, getCategory } from '../../logging';
import { EventsService, ChangeEvent } from './events.service';

const category = getCategory('AbstractStore');

// Set to a number of seconds to simulate latency and do page styling for loading states
const LOADING_TIME = 0;

// Set to true to simulate empty state
const EMPTY_STATE = false;

// prettier-ignore
export abstract class AbstractStore<
  T extends BaseEntity,
  L extends Array<T>,
  R extends RESTService<T, L>> {
  private _list: BehaviorSubject<L>;

  private _current: BehaviorSubject<T>;
  private currentSub: Subscription;
  private currentId: string;

  private _loading: BehaviorSubject<boolean> = new BehaviorSubject(false);

  private changeEvents: Observable<ChangeEvent>;

  constructor(
    public service: R,
    private eventService: EventsService,
    initialList: L,
    initialCurrent: T,
  ) {
    this._list = new BehaviorSubject<L>(initialList);
    this._current = new BehaviorSubject<T>(initialCurrent);
    this.changeEvents = this.eventService.changeEvents.filter(x => {
      return x.kind === this.service.kind;
    });
    this.currentSub = this._current.asObservable().subscribe(current => {
      if (!current) {
        this.currentId = undefined;
        return;
      }
      this.currentId = current.id;
    });
  }

  protected abstract get kind(): string;

  get list() {
    // Give back the _list,
    // but also update it if we get notified the a change occurred.
    return Observable.merge(
      this._list,
      this.changeEvents.flatMap(event => {
        // We could probably get fancy one day an only fetch the entry that matches event.id
        // simulate no data
        if (EMPTY_STATE) {
          return Observable.of([]);
        } else {
          return this.service.list();
        }
      }),
    ).share();
  }

  get resource() {
    return Observable.merge(
      this._current,
      this.changeEvents.filter(event => {
        return event.id === this.currentId;
      }).flatMap(event => {
        return this.service.get(event.id);
      }),
    );
  }

  get loading() {
    return this._loading.asObservable();
  }

  loadAll(retries = 0) {
    this._loading.next(true);
    this.service.list().subscribe(
      list => {
        setTimeout(() => {
          // simulate no elements
          if (EMPTY_STATE) {
            this._list.next(<L>[]);
          } else {
            this._list.next(list);
          }
          this._loading.next(false);
        }, LOADING_TIME);
      },
      error => {
        error = this.massageError(error);
        log.debugc(
          () => 'Error retrieving ' + plural(this.kind) + ': ' + error,
          category,
        );
        if (retries < 3) {
          setTimeout(() => {
            this.loadAll(retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._loading.next(false);
        }
      },
    );
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

  // Use clear() to clear the current resource so views don't show stale data
  clear(): void {
    this._current.next(undefined);
    this._loading.next(true);
  }

  load(id: string, retries = 0): Observable<T> {
    this._loading.next(true);
    this.service.get(id).subscribe(
      entity => {
        setTimeout(() => {
          this._current.next(this.plain(entity));
          this._loading.next(false);
        }, LOADING_TIME);
      },
      error => {
        error = this.massageError(error);
        log.debugc(
          () => 'Error retrieving ' + this.kind + ': ' + error,
          category,
        );
        if (retries < 3) {
          setTimeout(() => {
            this.load(id, retries + 1);
          }, (retries + 1) * 1000);
        } else {
          this._loading.next(false);
        }
      },
    );
    return this._current.asObservable();
  }

  create(entity: T): Observable<T> {
    const created = new Subject<T>();
    this.service.create(entity).subscribe(
      e => {
        created.next(this.plain(e));
      },
      error => {
        error = this.massageError(error);
        log.debugc(
          () =>
            'Error creating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        created.error(error);
      },
    );
    return created.share();
  }

  update(entity: T, reload = false): Observable<T> {
    if (reload) {
      this._loading.next(true);
    }
    const updated = new Subject<T>();
    this.service.update(entity).subscribe(
      e => {
        // PUT may not return a response, we'll massage it
        if (Array.isArray(e)) {
          e = entity;
        }
        updated.next(this.plain(e));
        this._loading.next(false);
      },
      error => {
        error = this.massageError(error);
        log.debugc(
          () =>
            'Error updating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        updated.error(error);
        this._loading.next(false);
      },
    );
    return updated.share();
  }

  updateOrCreate(entity: T): Observable<T> {
    if (entity.id) {
      return this.update(entity);
    } else {
      return this.create(entity);
    }
  }

  delete(entity: T): Observable<T> {
    const deleted = new Subject<T>();
    this.service.delete(entity).subscribe(
      e => {
        deleted.next(this.plain(e));
      },
      error => {
        error = this.massageError(error);
        log.debugc(
          () =>
            'Error updating ' +
            this.kind +
            ' (' +
            JSON.stringify(entity, null, 2) +
            ')' +
            ': ' +
            error,
          category,
        );
        deleted.error(error);
      },
    );
    return deleted.share();
  }

  private massageError(error: any) {
    let errorMessage: any;
    switch (typeof error) {
      case 'object':
      errorMessage = error;
      break;
      case 'string':
        try {
          errorMessage = JSON.parse(error);
        } catch (err) {
          // some random text back from the server :-(
          errorMessage = { error: error };
        }
        break;
      default:
        errorMessage = { error: error };
    }

    return errorMessage;
  }

  private plain(entity: T): T {
    if ('plain' in entity) {
      return (<any>entity).plain();
    } else {
      return entity;
    }
  }
}
