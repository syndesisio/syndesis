export interface BasicFilter {
  configuredProperties: {
    type: string;
    predicate: any;
    simple?: string;
    rules?: Rule[];
  };
}

export interface Rule {
  path: string;
  op?: string;
  value: string;
}
