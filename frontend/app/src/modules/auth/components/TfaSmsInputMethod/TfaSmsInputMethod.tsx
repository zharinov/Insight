import React, { MutableRefObject, useEffect, useRef, useState } from 'react';
import type { CodeValidityDTO } from '@rebrowse/types';
import { Block } from 'baseui/block';
import { toaster } from 'baseui/toast';
import { Flex, CodeInput, FlexColumn, Button } from '@rebrowse/elements';

import { TfaInputMethodProps } from '../types';

type Props = TfaInputMethodProps & {
  sendCode: () => Promise<CodeValidityDTO>;
};

export const TfaSmsInputMethod = ({
  code,
  handleChange,
  error,
  sendCode,
}: Props) => {
  const [validitySeconds, setValiditySeconds] = useState(0);
  const countdownInterval = useRef(null) as MutableRefObject<number | null>;
  const [isSendingCode, setIsSendingCode] = useState(false);

  useEffect(() => {
    if (countdownInterval.current !== null && validitySeconds === 0) {
      clearInterval(countdownInterval.current);
      countdownInterval.current = null;
    }
  }, [validitySeconds, countdownInterval]);

  const handleActionClick = (
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => {
    event.stopPropagation();
    event.preventDefault();
    if (validitySeconds !== 0 || isSendingCode) {
      return;
    }

    setIsSendingCode(true);

    sendCode()
      .then((response) => {
        toaster.positive('Success', {});
        setValiditySeconds(response.validitySeconds);

        countdownInterval.current = window.setInterval(() => {
          setValiditySeconds((v) => v - 1);
        }, 1000);
      })
      .catch(() => toaster.negative('Something went wrong', {}))
      .finally(() => setIsSendingCode(false));
  };

  return (
    <Flex>
      <Block>
        <CodeInput
          label="Mobile verification code"
          code={code}
          handleChange={handleChange}
          error={error}
        />
      </Block>
      <FlexColumn justifyContent={error ? 'center' : 'flex-end'} width="100%">
        <Button onClick={handleActionClick} isLoading={isSendingCode}>
          {validitySeconds ? `${validitySeconds}s` : 'Send Code'}
        </Button>
      </FlexColumn>
    </Flex>
  );
};
