import { LoadingPage } from '@rh-uxd/integration-react';
import React, { Suspense } from 'react';
import './AppLayout.css';


export const AppLayout: React.FunctionComponent = ({...props}) => {

  const [isLoading, setIsLoading] = React.useState(true);
  const PageComponent = React.lazy(() => import(('./AppPage')));

  const delayState = () => {
    setTimeout(() => {
      setIsLoading(false);
    }, 2000);
  };

  delayState();

  return (
    <div style={{position: 'relative'}}>
      <Suspense fallback={ isLoading && <LoadingPage appName="Syndesis"/>}>
        <PageComponent {...props}/>
      </Suspense>
    </div>
  );
};
