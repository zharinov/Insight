import React from 'react';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { Session } from '@rebrowse/types';
import DevTools from 'modules/sessions/containers/DevTools';
import { FlexColumn } from '@rebrowse/elements';

type Props = {
  session: Session;
};

export const SessionDetails = ({ session }: Props) => {
  return (
    <>
      <Block display="flex" height="100%">
        <FlexColumn overflow="hidden" width="100%">
          <H3>Session {session.id}</H3>
          <div>Device ID: {session.deviceId}</div>
        </FlexColumn>
      </Block>
      <DevTools sessionId={session.id} />
    </>
  );
};
