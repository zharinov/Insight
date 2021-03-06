import React, { useState } from 'react';
import { Modal, ModalHeader, ModalFooter, ModalBody } from 'baseui/modal';
import { Button } from '@rebrowse/elements';
import type {
  APIError,
  APIErrorDataResponse,
  SsoSetupDTO,
} from '@rebrowse/types';
import FormError from 'shared/components/FormError';
import { SIZE } from 'baseui/button';
import { AuthApi } from 'api';
import { toaster } from 'baseui/toast';
import { Block } from 'baseui/block';

type Props = {
  label: string;
  isOpen: boolean;
  onClose: () => void;
  setActiveSetup: (setup: SsoSetupDTO | undefined) => void;
};

export const SsoSetupDisableModal = ({
  label,
  isOpen,
  onClose,
  setActiveSetup,
}: Props) => {
  const [formError, setFormError] = useState<APIError | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const onSubmit = () => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);
    setFormError(undefined);

    AuthApi.sso.setup
      .delete()
      .then(() => {
        toaster.positive(`${label} SSO setup disabled`, {});
        setActiveSetup(undefined);
        onClose();
      })
      .catch(async (setupError) => {
        const errorDTO: APIErrorDataResponse = await setupError.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalHeader>Disable {label} authentication</ModalHeader>
      <ModalBody>
        <Block>
          Single Sign-On (or SSO) allows you to manage your organization’s
          entire membership via a third-party provider.
        </Block>
      </ModalBody>
      <ModalFooter>
        <Button kind="tertiary" onClick={onClose} size={SIZE.compact}>
          Maybe later
        </Button>
        <Button
          size={SIZE.compact}
          isLoading={isSubmitting}
          $style={{ marginLeft: '8px' }}
          onClick={onSubmit}
        >
          Disable
        </Button>
      </ModalFooter>
      {formError && <FormError error={formError} />}
    </Modal>
  );
};
