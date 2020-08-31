import { Modal, SIZE } from 'baseui/modal';
import React, { useCallback, useState } from 'react';
import { ProgressSteps, Step } from 'baseui/progress-steps';
import { PhoneNumber, User } from '@insight/types';
import { UpdateUserPayload } from '@insight/sdk/dist/auth';

import SetPhoneNumberForm from './SetPhoneNumberForm';
import VerifyPhoneNumberForm from './VerifyPhoneNumberForm';

type Props = {
  isOpen: boolean;
  setIsModalOpen: (isOpen: boolean) => void;
  phoneNumber: PhoneNumber | null;
  updateUser: (userUpdatePayload: UpdateUserPayload) => Promise<User>;
  updateUserCache: (user: User) => void;
};

const ConfigurePhoneNumberModal = ({
  isOpen,
  setIsModalOpen,
  phoneNumber,
  updateUser,
  updateUserCache,
}: Props) => {
  const [currentStep, setCurrentStep] = useState(0);

  const onPhoneNumberSet = useCallback(() => {
    setCurrentStep((s) => s + 1);
  }, []);

  const onBack = useCallback(() => {
    setCurrentStep((s) => s - 1);
  }, []);

  const onPhoneNumberVerified = useCallback(
    (user: User) => {
      updateUserCache(user);
      setIsModalOpen(false);
    },
    [setIsModalOpen, updateUserCache]
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => setIsModalOpen(false)}
      size={SIZE.auto}
    >
      <ProgressSteps current={currentStep}>
        <Step title="Set phone number">
          <SetPhoneNumberForm
            phoneNumber={phoneNumber}
            onPhoneNumberSet={onPhoneNumberSet}
            updateUser={updateUser}
          />
        </Step>

        <Step title="Verify phone number">
          <VerifyPhoneNumberForm
            onBack={onBack}
            onPhoneNumberVerified={onPhoneNumberVerified}
          />
        </Step>
      </ProgressSteps>
    </Modal>
  );
};

export default React.memo(ConfigurePhoneNumberModal);
