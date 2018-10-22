export interface ICustomResourceDefinition {
  metadata: {
    name: string,
    selfLink: string,
    uid: string,
    resourceVersion: string,
    generation: number,
    creationTimestamp: string
  },
  spec: {
    group: string,
    version: string,
    names: {
      plural: string,
      singular: string,
      kind: string,
      listKind: string,
    },
    scope: string
  },
  status: {
    conditions: Array<{
      type: string,
      status: string,
      lastTransitionTime: string,
      reason: string,
      message: string,
    }>,
    acceptedNames: {
      plural: string,
      singular: string,
      kind: string,
      listKind: string,
    }
  }
}

export interface ICustomResource {
  apiVersion: string;
  kind: string;
  metadata: any,
  spec: any,
  status: {
    phase: string;
    version: string;
  }
}

export interface IProject {
  apiVersion: string;
  kind: string;
  metadata: any,
  spec: any,
  status: {
    phase: string;
    version: string;
  }
}

export interface IPod {
  'metadata': {
    'name': string;
    'generateName': string;
    'namespace': string;
    'selfLink': string;
    'uid': string;
    'resourceVersion': string;
    'creationTimestamp': string;
    'labels': {
      'camel.apache.org/integration': string;
      'pod-template-hash': string;
    },
    'annotations': {
      'openshift.io/scc': string;
    },
    'ownerReferences': [
      {
        'apiVersion': string;
        'kind': string;
        'name': string;
        'uid': string;
        'controller': boolean,
        'blockOwnerDeletion': boolean
      }
      ]
  },
  'spec': {
    'volumes': [
      {
        'name': string;
        'configMap': {
          'name': string;
          'items': [
            {
              'key': string;
              'path': string;
            },
            {
              'key': string;
              'path': string;
            }
            ],
          'defaultMode': number
        }
      },
      {
        'name': string;
        'secret': {
          'secretName': string;
          'defaultMode': number
        }
      }
      ],
    'containers': [
      {
        'name': string;
        'image': string;
        'env': [
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          },
          {
            'name': string;
            'value': string;
          }
          ],
        'resources': {},
        'volumeMounts': [
          {
            'name': string;
            'mountPath': string;
          },
          {
            'name': string;
            'readOnly': boolean,
            'mountPath': string;
          }
          ],
        'terminationMessagePath': string;
        'terminationMessagePolicy': string;
        'imagePullPolicy': string;
        'securityContext': {
          'capabilities': {
            'drop': string[]
          },
          'runAsUser': number
        }
      }
      ],
    'restartPolicy': string;
    'terminationGracePeriodSeconds': number,
    'dnsPolicy': string;
    'serviceAccountName': string;
    'serviceAccount': string;
    'nodeName': string;
    'securityContext': {
      'seLinuxOptions': {
        'level': string;
      },
      'fsGroup': number
    },
    'imagePullSecrets': [
      {
        'name': string;
      }
      ],
    'schedulerName': string;
  },
  'status': {
    'phase': string;
    'conditions': [
      {
        'type': string;
        'status': string;
        'lastProbeTime': null,
        'lastTransitionTime': string;
      },
      {
        'type': string;
        'status': string;
        'lastProbeTime': null,
        'lastTransitionTime': string;
      },
      {
        'type': string;
        'status': string;
        'lastProbeTime': null,
        'lastTransitionTime': string;
      }
      ],
    'hostIP': string;
    'podIP': string;
    'startTime': string;
    'containerStatuses': [
      {
        'name': string;
        'state': {
          'running': {
            'startedAt': string;
          }
        },
        'lastState': {},
        'ready': boolean,
        'restartCount': 0,
        'image': string;
        'imageID': string;
        'containerID': string;
      }
      ],
    'qosClass': string;
  }
}

export interface IResource {
  kind: string;
  id: string;
}

export interface IIntegration {
  board: IIntegrationBoard;
  createdAt: number;
  currentState: 'Published' | 'Unpublished' | 'Pending' | 'Error';
  deploymentVersion: number;
  deployments: any[]
  flows: any[];
  id: string;
  isDraft: boolean;
  name: string;
  resources: IResource[]
  tags: string[];
  targetState: string;
  updatedAt: number;
  url: string;
  version: number;
}

export interface IIntegrationBoard {
  createdAt: number;
  errors: number;
  id: string;
  metadata: { [id: string]: number; };
  notices: number;
  targetResourceId: string;
  updatedAt: number;
  warnings: number;
}

export interface IIntegrationsMetrics {
  errors: number;
  lastProcessed: number;
  messages: number;
  metricsProvider: string;
  start: number;
  topIntegrations: Array<{
    [id: string]: number;
  }>;
}

export interface IMonitoredIntegration {
  integration: IIntegration;
  monitoring?: IIntegrationMonitoring;
}

export interface IIntegrationMonitoring {
  deploymentVersion: number;
  detailedState: {
    value: string;
    currentStep: number;
    totalSteps: number;
  }
  value: string;
  id: string;
  integrationId: string;
  linkType: string;
  namespace: string;
  podName: string;
}

export interface IConnection {
  board: {
    createdAt: number;
    updatedAt: number;
  };
  configuredProperties?: { [key: string]: any; };
  connector: any;
  connectorId: string;
  description: string;
  icon: string;
  id: string;
  isDerived: boolean;
  name: string;
  tags: string[];
  uses: number;
}
