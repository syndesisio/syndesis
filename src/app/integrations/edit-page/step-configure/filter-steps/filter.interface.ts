export interface BasicFilter {
  id?: string;
  stepKind: string;
  configuredProperties: {
    type: string;
    predicate: string;
    simple: string;
    rules?: Rule[];
  };
}

export interface Rule {
  path: string;
  op?: string;
  value: string;
}
