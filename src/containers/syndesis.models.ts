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
  "metadata": {
    "name": string;
    "generateName": string;
    "namespace": string;
    "selfLink": string;
    "uid": string;
    "resourceVersion": string;
    "creationTimestamp": string;
    "labels": {
      "camel.apache.org/integration": string;
      "pod-template-hash": string;
    },
    "annotations": {
      "openshift.io/scc": string;
    },
    "ownerReferences": [
      {
        "apiVersion": string;
        "kind": string;
        "name": string;
        "uid": string;
        "controller": boolean,
        "blockOwnerDeletion": boolean
      }
      ]
  },
  "spec": {
    "volumes": [
      {
        "name": string;
        "configMap": {
          "name": string;
          "items": [
            {
              "key": string;
              "path": string;
            },
            {
              "key": string;
              "path": string;
            }
            ],
          "defaultMode": number
        }
      },
      {
        "name": string;
        "secret": {
          "secretName": string;
          "defaultMode": number
        }
      }
      ],
    "containers": [
      {
        "name": string;
        "image": string;
        "env": [
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          },
          {
            "name": string;
            "value": string;
          }
          ],
        "resources": {},
        "volumeMounts": [
          {
            "name": string;
            "mountPath": string;
          },
          {
            "name": string;
            "readOnly": boolean,
            "mountPath": string;
          }
          ],
        "terminationMessagePath": string;
        "terminationMessagePolicy": string;
        "imagePullPolicy": string;
        "securityContext": {
          "capabilities": {
            "drop": string[]
          },
          "runAsUser": number
        }
      }
      ],
    "restartPolicy": string;
    "terminationGracePeriodSeconds": number,
    "dnsPolicy": string;
    "serviceAccountName": string;
    "serviceAccount": string;
    "nodeName": string;
    "securityContext": {
      "seLinuxOptions": {
        "level": string;
      },
      "fsGroup": number
    },
    "imagePullSecrets": [
      {
        "name": string;
      }
      ],
    "schedulerName": string;
  },
  "status": {
    "phase": string;
    "conditions": [
      {
        "type": string;
        "status": string;
        "lastProbeTime": null,
        "lastTransitionTime": string;
      },
      {
        "type": string;
        "status": string;
        "lastProbeTime": null,
        "lastTransitionTime": string;
      },
      {
        "type": string;
        "status": string;
        "lastProbeTime": null,
        "lastTransitionTime": string;
      }
      ],
    "hostIP": string;
    "podIP": string;
    "startTime": string;
    "containerStatuses": [
      {
        "name": string;
        "state": {
          "running": {
            "startedAt": string;
          }
        },
        "lastState": {},
        "ready": boolean,
        "restartCount": 0,
        "image": string;
        "imageID": string;
        "containerID": string;
      }
      ],
    "qosClass": string;
  }
}

export interface IResource {
  kind: string;
  id: string;
}

export interface IIntegration {
  board: any;
  createdAt: number;
  currentState: string;
  deploymentVersion: number;
  deployments: any[]
  flows: any[];
  id: boolean;
  isDraft: boolean;
  name: string;
  resources: IResource[]
  tags: string[];
  targetState: string;
  updatedAt: number;
  url: string;
  version: number;
}