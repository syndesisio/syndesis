import { text, withKnobs } from '@storybook/addon-knobs';
import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AboutModal, AboutModalContent } from '../../src';
import { withState } from '@dump247/storybook-state';
const stories = storiesOf('Shared/About Modal', module);
stories.addDecorator(withKnobs);
const modalToggleLogger = action('logging modal toggle');
stories.add(
  'AboutModal',
  withState({ isOpen: false })(({ store }) => {
    function handleModalToggle() {
      store.set({ isOpen: !store.state.isOpen });
      modalToggleLogger();
    }
    return (
      <>
        <button
          className="pf-c-button pf-m-primary"
          onClick={handleModalToggle}
        >
          Open Modal
        </button>
        <AboutModal
          bgImg={undefined}
          trademark={text(
            'trademark',
            'A flexible, customizable, open source platform that provides core integration capabilities as a service.'
          )}
          productName={text('productName', 'Syndesis')}
          isModalOpen={store.state.isOpen}
          handleModalToggle={() => handleModalToggle()}
          brandImg={'https://avatars0.githubusercontent.com/u/23079786'}
        >
          <AboutModalContent
            productName="Syndesis"
            version={'1.8-SNAPSHOT'}
            commitId={'dd8b5445fd74f956147eb0a21870d0d5c3e0fb69'}
            buildId={'60dfad7e-fba5-49e9-b393-e806a135299e'}
            i18nBuildIdLabel={'Build ID:'}
            i18nCommitIdLabel={'Commit ID:'}
          />
        </AboutModal>
      </>
    );
  })
);
