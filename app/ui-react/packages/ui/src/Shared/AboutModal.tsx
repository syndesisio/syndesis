import { AboutModal as PfAboutModal } from '@patternfly/react-core';
import * as React from 'react';

export interface IAboutModal {
  bgImg?: string;
  brandImg: any;
  handleModalToggle: any;
  isModalOpen: boolean;
  trademark: string;
  productName: string;
  children: React.ReactNode;
}

export class AboutModal extends React.Component<IAboutModal> {
  constructor(props: IAboutModal) {
    super(props);
  }

  public render() {
    const {
      bgImg,
      brandImg,
      children,
      isModalOpen,
      handleModalToggle,
      productName,
      trademark,
    } = this.props;
    return (
      <React.Fragment>
        <PfAboutModal
          isOpen={isModalOpen}
          onClose={handleModalToggle}
          trademark={trademark}
          brandImageSrc={brandImg}
          brandImageAlt="Brand Image"
          productName={productName}
          backgroundImageSrc={bgImg}
        >
          {children}
        </PfAboutModal>
      </React.Fragment>
    );
  }
}
