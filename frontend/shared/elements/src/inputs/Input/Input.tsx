import React, { forwardRef } from 'react';
import { Input as BaseUiInput, InputProps } from 'baseui/input';

import { expandBorderRadius } from '../../helpers';

type Props = InputProps;

export const inputBorderRadius = expandBorderRadius('8px');

export const Input = forwardRef<HTMLInputElement, Props>(
  ({ overrides, ...rest }, ref) => {
    return (
      <BaseUiInput
        overrides={{
          Root: { style: inputBorderRadius },
          ...overrides,
        }}
        inputRef={ref}
        {...rest}
      />
    );
  }
);
