import {
  Dropdown,
  DropdownItem,
  KebabToggle,
  Split,
  SplitItem,
  Tooltip,
} from '@patternfly/react-core';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink, IButtonLinkProps } from '../../Layout';
import './VirtualizationActions.css';

/**
 * Actions whose UI is a button.
 * @property {string} iconClassName - a Patternfly or FontAwesome icon (ex., 'pf pficon-spinner2')
 * @property {string} id - a unique identifier of the the action
 * @property {string | JSX.Element} i18nLabel - the localized text displayed on the button
 * @property {string} i18nToolTip - the localized text for the button tooltip
 */
export interface IVirtualizationAction extends IButtonLinkProps {
  iconClassName?: string;
  id: string;
  i18nLabel: string | JSX.Element;
  i18nToolTip?: string;
}

export interface IVirtualizationActionsProps {
  /**
   * The actions used by the buttons. If `undefined` the default actions will be used. If no actions are
   * wanted, use an empty array.
   */
  actions?: IVirtualizationAction[];

  /**
   * The actions used by the kebab menu items. If `undefined` the default actions will be used. If no actions are
   * wanted, use an empty array.
   */
  items?: IVirtualizationAction[];

  /**
   * The name of the virtualization is used when forming the kebab menu identifier,
   */
  virtualizationName: string;
}

/**
 * A row of buttons and a kebab menu.
 * @param props the properties defining the actions being used.
 */
export const VirtualizationActions: React.FunctionComponent<
  IVirtualizationActionsProps
> = props => {
  const [isOpen, setOpen] = React.useState(false);

  const createButton = (action: IVirtualizationAction): JSX.Element => {
    const {
      id: dataTestId,
      i18nLabel,
      i18nToolTip,
      iconClassName,
      ...otherProps
    } = action;
    if (action.i18nToolTip) {
      return (
        <Tooltip content={i18nToolTip} position={'auto'}>
          <div className="pf-u-display-inline-block">
            <ButtonLink
              className={'btn'}
              data-testid={`virtualization-actions-${toValidHtmlId(dataTestId)}`}
              {...otherProps}
            >
              {iconClassName && <span className={'pf pficon-spinner2'}>&nbsp;</span>}
              {i18nLabel}
            </ButtonLink>
            &nbsp;&nbsp;
          </div>
        </Tooltip>
      );
    }

    return (
      <>
        <ButtonLink
          className={'btn'}
          data-testid={`virtualization-actions-${toValidHtmlId(dataTestId)}`}
          {...otherProps}
        >
          {iconClassName && <span className={'pf pficon-spinner2'}>&nbsp;</span>}
          {i18nLabel}
        </ButtonLink>
        &nbsp;&nbsp;
      </>
    );
  };

  const getButtons = (): JSX.Element[] | undefined => {
    if (props.actions && props.actions.length !== 0) {
      return props.actions.map((action, index) => {
        return <SplitItem key={index}>{createButton(action)}</SplitItem>;
      });
    }

    return undefined;
  };

  const getKebab = (): JSX.Element | undefined => {
    if (props.items && props.items.length !== 0) {
      return (
        <SplitItem>
          <Dropdown
            direction={'down'}
            dropdownItems={props.items.map((item, index) => {
              return item.href ? (
                <DropdownItem
                  className={'virtualization-actions__menuItem'}
                  component={'button'}
                  data-testid={`virtualization-actions-${toValidHtmlId(item.id)}`}
                  isDisabled={item.disabled}
                  key={index}
                >
                  <Link to={item.href}>{item.i18nLabel}</Link>
                </DropdownItem>
              ) : (
                <DropdownItem
                  className={'virtualization-actions__menuItem'}
                  component={'button'}
                  data-testid={`virtualization-actions-${toValidHtmlId(item.id)}`}
                  isDisabled={item.disabled}
                  key={index}
                  onClick={item.onClick}
                >
                  {item.i18nLabel}
                </DropdownItem>
              );
            })}
            id={`virtualization-${toValidHtmlId(props.virtualizationName)}-kebab-menu`}
            isOpen={isOpen}
            isPlain={true}
            onSelect={onSelect}
            position={'right'}
            toggle={<KebabToggle onToggle={onToggle} />}
          />
        </SplitItem>
      );
    }

    return undefined;
  };

  const onSelect = () => {
    setOpen(!isOpen);
  };

  const onToggle = (open: boolean) => {
    setOpen(open);
  };

  return (
    <Split>
      {props.actions && getButtons()}
      {props.items && getKebab()}
    </Split>
  );
};
