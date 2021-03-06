import React from 'react';
import {
  fullHeightDecorator,
  configureStory,
  mockApiError,
} from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import type { ResponsePromise } from 'ky';
import { TFA_METHODS } from 'test/data';
import type { Meta } from '@storybook/react';

import { VerificationPage } from './VerificationPage';

export default {
  title: 'auth/pages/VerificationPage',
  component: VerificationPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa.challenge, 'complete')
        .resolves({} as Response),
    };
  },
});

export const WithTotpOnly = () => {
  return <VerificationPage methods={['totp']} />;
};
WithTotpOnly.story = Base.story;

export const WithSmsOnly = () => {
  return <VerificationPage methods={['sms']} />;
};
WithSmsOnly.story = Base.story;

export const WithMissingChallengeIdError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithMissingChallengeIdError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa.challenge, 'complete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'Bad Request',
            errors: {
              challengeId: 'Required',
            },
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as ResponsePromise;
        }),
    };
  },
});

export const WithExpiredChallengeError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithExpiredChallengeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa.challenge, 'complete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'TFA challenge session expired',
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as ResponsePromise;
        }),
    };
  },
});

export const WithInvalidCodeError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithInvalidCodeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa.challenge, 'complete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'Bad Request',
            errors: {
              code: 'Invalid code',
            },
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as ResponsePromise;
        }),
    };
  },
});
