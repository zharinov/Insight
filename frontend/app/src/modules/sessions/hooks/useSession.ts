import { SessionApi } from 'api';
import { mapSession } from '@rebrowse/sdk';
import { useEffect, useMemo } from 'react';
import type { SessionDTO } from '@rebrowse/types';
import useSWRQuery from 'shared/hooks/useSWRQuery';

export const useSession = (sessionId: string, initialData: SessionDTO) => {
  const { data = initialData, mutate } = useSWRQuery(
    'SessionApi.getSession',
    () => SessionApi.getSession(sessionId),
    { initialData }
  );

  // TODO: cache key should probably include session id
  useEffect(() => {
    mutate(initialData);
  }, [initialData, mutate]);

  const session = useMemo(() => mapSession(data), [data]);

  return { session };
};
