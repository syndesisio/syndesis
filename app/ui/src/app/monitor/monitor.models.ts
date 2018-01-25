import { BaseReducerModel } from '@syndesis/ui/platform';

export interface MonitorState extends BaseReducerModel {
  metrics: any;        // TODO: Assign proper <T> once we define a final Metrics model
  logs: Array<any>;    // TODO: Assign proper <T> once we define a final Logs model
}
