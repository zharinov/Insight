import React, { useMemo } from 'react';
import { Card, StyledAction, StyledBody } from 'baseui/card';
import { H4, ParagraphSmall } from 'baseui/typography';
import { Block } from 'baseui/block';
import Divider from 'shared/components/Divider';
import { SpacedBetween } from '@insight/elements';
import { ProgressBar, ProgressBarProps } from 'baseui/progress-bar';
import { useStyletron } from 'baseui';
import { format } from 'date-fns';
import { Button, SHAPE, SIZE } from 'baseui/button';
import type { Plan } from '@insight/types';

type Props = {
  resetsOn?: Date;
  plan?: Plan['type'];
  sessionsUsed?: number;
  dataRetention?: Plan['dataRetention'];
  isLoading: boolean;
  onUpgradeClick: (
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => void;
};

const YourPlan = ({
  plan,
  sessionsUsed,
  resetsOn,
  isLoading,
  dataRetention,
  onUpgradeClick,
}: Props) => {
  const [_css, theme] = useStyletron();

  const {
    colors: { negative400: planViolationErrorColor },
  } = theme;

  const totalSessions = plan === 'free' ? 1000 : undefined;

  const violatesPlan =
    sessionsUsed && totalSessions !== undefined && sessionsUsed > totalSessions;

  const totalSessionsText =
    totalSessions === undefined ? '\u221e' : totalSessions.toLocaleString();

  const progressBarProps: ProgressBarProps = useMemo(
    () =>
      totalSessions === undefined || sessionsUsed === undefined
        ? { infinite: true }
        : {
            value: (sessionsUsed / totalSessions) * 100,
            successValue: 100,
          },
    [sessionsUsed, totalSessions]
  );

  return (
    <Card title="Your plan">
      <Divider />
      <StyledBody>
        <SpacedBetween>
          <H4 marginTop={0} marginBottom={theme.sizing.scale500}>
            Insight {plan ? plan[0].toUpperCase() + plan.substring(1) : ''}
          </H4>
          <Block flex={1} maxWidth="400px">
            <ProgressBar
              {...progressBarProps}
              showLabel
              overrides={{
                Label: {
                  style: {
                    textAlign: 'right',
                    marginRight: theme.sizing.scale500,
                  },
                },
                BarProgress: {
                  style: {
                    background: violatesPlan
                      ? planViolationErrorColor
                      : undefined,
                  },
                },
              }}
              getProgressLabel={() => (
                <>
                  <span>
                    {sessionsUsed} of {totalSessionsText} sessions
                  </span>
                  <br />
                  {resetsOn && (
                    <span>
                      {`Resets ${format(resetsOn, 'MMM d, yyyy')} at ${format(
                        resetsOn,
                        'h:mma'
                      )}`}
                    </span>
                  )}
                </>
              )}
            />
          </Block>
        </SpacedBetween>
        <Block>
          <ParagraphSmall>
            Sessions: <b>{totalSessionsText}/mo</b>
          </ParagraphSmall>
        </Block>
        <Block>
          <ParagraphSmall>
            Data history: <b>{dataRetention}</b>
          </ParagraphSmall>
        </Block>
      </StyledBody>

      {plan !== 'enterprise' && (
        <>
          <Divider />
          <StyledAction>
            <Button
              size={SIZE.compact}
              shape={SHAPE.pill}
              $style={{ width: '100%', maxWidth: '300px' }}
              onClick={onUpgradeClick}
              disabled={isLoading}
            >
              Upgrade
            </Button>
          </StyledAction>
        </>
      )}
    </Card>
  );
};

export default React.memo(YourPlan);
