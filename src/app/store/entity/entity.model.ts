export interface BaseEntity {
  id?: string;
}

export interface Entities<T extends BaseEntity> {
  ids: string[];
  entities: { [id: string]: T };
  loaded?: boolean;
  loading?: boolean;
  selectedEntityId?: string;
};

export function initialEntities<T extends BaseEntity>(vals: any = {}): Entities<T> {
  return Object.assign({
    ids: [],
    entities: {},
    loaded: false,
    loading: false,
    selectedEntityId: null,
  }, vals);
};

export interface IDs {
  loaded: boolean;
  loading: boolean;
  ids: string[];
};

export function initialIDs(): IDs {
  return {
    loaded: false,
    loading: false,
    ids: [],
  };
};
