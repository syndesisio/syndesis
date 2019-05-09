import * as React from 'react';
export interface IDownloadDiagnosticsProps {
  children: React.ReactNode;
}

export const DownloadDiagnostics: React.FunctionComponent<
  IDownloadDiagnosticsProps
> = ({ children }) => (
  <>
    <div className="pf-c-content">
      <h2 className="pf-c-title pf-m-lg">
        Download Troubleshooting Diagnostics
      </h2>
      <p>
        System level and application level diagnostics will be captured since
        both are required to troubleshoot any issues. Usernames and passwords
        are not present in the downloaded zip file.
      </p>
      {children}
    </div>
  </>
);
