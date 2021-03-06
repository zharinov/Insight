import React from 'react';
import { render } from 'test/utils';
import { StoryConfiguration } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import userEvent from '@testing-library/user-event';
import { RenderableComponent } from '@rebrowse/next-testing';

import {
  Base,
  WithSetupStartError,
  WithInvalidCodeError,
  WithQrCodeExpiredError,
} from './TimeBasedMultiFactorAuthenticationSetupModal.stories';

describe('<TimeBasedTwoFactorAuthenticationSetupModal />', () => {
  const renderTfaSetupModal = <Props, T, S extends StoryConfiguration<T>>(
    component: RenderableComponent<Props, T, S>
  ) => {
    const result = render(component);

    const closeButton = result.container.querySelector(
      'button[aria-label="Close"]'
    ) as HTMLButtonElement;
    const codeInput = result.container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    const submitButton = result.getByText('Submit');
    return { submitButton, codeInput, closeButton, ...result };
  };

  it('Base', async () => {
    Base.story.setupMocks(sandbox);
    const onClose = sandbox.stub();
    const onTfaConfigured = sandbox.stub();

    const {
      submitButton,
      codeInput,
      findByText,
      queryByText,
      closeButton,
    } = renderTfaSetupModal(
      <Base onClose={onClose} onTfaConfigured={onTfaConfigured} />
    );

    userEvent.click(closeButton);
    sandbox.assert.calledOnce(onClose);

    await userEvent.type(codeInput, '12');
    userEvent.click(submitButton);
    await findByText('Required');

    await userEvent.type(codeInput, '123456');
    expect(queryByText('Required')).toBeNull();

    await userEvent.click(submitButton);
    sandbox.assert.calledOnce(onTfaConfigured);
  });

  it('WithSetupStartError', async () => {
    WithSetupStartError.story.setupMocks(sandbox);
    const { submitButton, codeInput, findByText } = renderTfaSetupModal(
      <WithSetupStartError />
    );
    await findByText('Internal Server Error');

    expect(submitButton).toBeDisabled();
    expect(codeInput).toBeDisabled();
  });

  it('WithInvalidCodeError', async () => {
    WithInvalidCodeError.story.setupMocks(sandbox);
    const { submitButton, codeInput, findByText } = renderTfaSetupModal(
      <WithInvalidCodeError />
    );
    await userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);
    await findByText('Invalid code');

    expect(submitButton).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });

  it('WithQrCodeExpiredError', async () => {
    WithQrCodeExpiredError.story.setupMocks(sandbox);
    const { submitButton, codeInput, findByText } = renderTfaSetupModal(
      <WithQrCodeExpiredError />
    );
    await userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);
    await findByText('QR code expired');

    expect(submitButton).not.toBeDisabled();
    expect(codeInput).not.toBeDisabled();
  });
});
