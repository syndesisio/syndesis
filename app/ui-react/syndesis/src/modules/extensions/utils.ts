import { Extension } from '@syndesis/models';
import i18n from '../../i18n';

/**
 * Obtains a localized extension type name.
 * @param extension the extension whose type name is being requested
 */
export function getExtensionTypeName(extension: Extension) {
  const type = extension.extensionType;

  if ('Steps' === type) {
    return i18n.t('extensions:extension.StepExtension');
  }

  if ('Connectors' === type) {
    return i18n.t('extensions:extension.ConnectorExtension');
  }

  if ('Libraries' === type) {
    return i18n.t('extensions:extension.LibraryExtension');
  }

  return i18n.t('extensions:extension.unknownExtensionType');
}
