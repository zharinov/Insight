import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@rebrowse/testing';

import { Base, WithError } from './ChangePassword.stories';

describe('<ChangePassword />', () => {
  it('Should successfully change password', async () => {
    const changePasswordStub = Base.story.setupMocks(sandbox);
    const { getByPlaceholderText, getByText, findByText } = render(<Base />);
    await userEvent.type(
      getByPlaceholderText('Current password'),
      'currentPassword123'
    );
    await userEvent.type(getByPlaceholderText('New password'), 'password 123');
    await userEvent.type(
      getByPlaceholderText('Confirm new password'),
      'password 123'
    );

    userEvent.click(getByText('Save new password'));
    await findByText('Password changed');
    sandbox.assert.calledWithExactly(changePasswordStub, {
      currentPassword: 'currentPassword123',
      newPassword: 'password 123',
      confirmNewPassword: 'password 123',
    });
  });

  it('Should handle error', async () => {
    WithError.story.setupMocks(sandbox);
    const { getByPlaceholderText, getByText, findByText } = render(
      <WithError />
    );
    await userEvent.type(
      getByPlaceholderText('Current password'),
      'currentPassword123'
    );
    await userEvent.type(getByPlaceholderText('New password'), 'password 123');
    await userEvent.type(
      getByPlaceholderText('Confirm new password'),
      'password 123'
    );

    userEvent.click(getByText('Save new password'));
    await findByText('New password cannot be the same as the previous one!');
  });
});
