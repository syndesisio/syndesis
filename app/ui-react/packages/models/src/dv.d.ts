// TODO remove when these values are advertised by the swagger
export interface RestDataService {
  connections: number;
  drivers: number;
  keng___links: [];
  keng__baseUri: string;
  keng__dataPath: string;
  keng__hasChildren: boolean;
  keng__id: string;
  keng__kType: string;
  publishedState:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  serviceVdbName: string;
  serviceVdbVersion: string;
  serviceViewDefinitions: string[];
  serviceViewModel: string;
  tko__description: string;
}

export interface RestViewDefinition {
  keng__baseUri: string;
  keng__id: string;
  keng__dataPath: string;
  keng__kType: string;
  keng__hasChildren: boolean;
  keng___links: [];
}
