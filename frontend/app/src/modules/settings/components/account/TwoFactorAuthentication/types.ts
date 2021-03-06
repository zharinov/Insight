import { User, TfaMethod, TfaSetupDTO } from '@rebrowse/types';

export type Props = {
  user: User;
};

export type TwoFactorAuthenticationProps = {
  setupsMaps: Record<TfaMethod, TfaSetupDTO>;
  setupDisabled: boolean;
  onMethodDisabled: () => void;
  onMethodEnabled: (setup: TfaSetupDTO) => void;
};
