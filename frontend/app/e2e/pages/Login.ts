import { getByPlaceholderText, getByText } from '@testing-library/testcafe';

import config from '../config';

export type LoginCredentials = { email: string; password: string };

class Login {
  public readonly path = `${config.appBaseURL}/login`;

  /* Selectors */
  public readonly emailInput = getByPlaceholderText('Email');
  public readonly passwordInput = getByPlaceholderText('Password');
  public readonly signInButton = getByText('Sign in');
  public readonly forgotPasswordButton = getByText('Forgot?');
  public readonly createFreeAccount = getByText('Create a free account');

  /* Utils */
  public login = (t: TestController, { email, password }: LoginCredentials) => {
    return t
      .typeText(this.emailInput, email)
      .typeText(this.passwordInput, password)
      .click(this.signInButton);
  };

  public loginWithInsightUser = (t: TestController) => {
    return this.login(t, {
      email: config.insightUserEmail,
      password: config.insightUserPassword,
    });
  };
}

export default new Login();